# Low-cost deployment: Oracle Cloud Always Free + Cloudflare Pages

This guide documents the steps required to deploy BudgetScope with:

- **Java/Micronaut backend** on an **Oracle Cloud Infrastructure (OCI) Always Free** VM.
- **PostgreSQL database** on the same VM. As an initial low-cost alternative, SQLite can be used later if the application is adapted for that driver.
- **Next.js frontend** on **Cloudflare Pages**.

> Official references consulted: Oracle documents Always Free AMD Micro and Ampere A1 instances, with the Ampere A1 Always Free allowance equivalent to 2 OCPU and 12 GB of memory in Always Free tenancies; Cloudflare documents deploying Next.js on Pages and configuring environment variables from `Workers & Pages > Settings > Environment variables`.

## 1. Target architecture

```text
User
  │
  ├── https://app.example.com        -> Cloudflare Pages -> apps/web
  │
  └── https://api.example.com        -> Oracle VM -> Caddy/Nginx -> Micronaut API :8080
                                            │
                                            └── PostgreSQL localhost:5432
```

Domain recommendations:

- Use `app.example.com` for the frontend.
- Use `api.example.com` for the backend.
- Keep PostgreSQL private: only expose it on `localhost` or an internal Docker network.

## 2. Prepare accounts and domain

1. Create an Oracle Cloud Free Tier account.
2. Create a Cloudflare account.
3. Buy or reuse a domain.
4. Move the domain nameservers to Cloudflare if you want to manage DNS and SSL there.
5. Create a remote Git repository that Cloudflare Pages can access.

## 3. Create the Always Free VM in Oracle Cloud

1. Open the **OCI Console**.
2. Go to **Compute > Instances > Create instance**.
3. Choose a stable Linux image, for example **Ubuntu 24.04 LTS**.
4. Choose an Always Free shape:
   - Preferred: **Ampere A1 Flex** with ARM64 architecture, for example 2 OCPU and 12 GB RAM if available within the free allowance.
   - Alternative: **VM.Standard.E2.1.Micro** AMD if Ampere capacity is not available.
5. Create or select a **Virtual Cloud Network** with a public subnet.
6. Add an SSH public key.
7. Confirm that the instance is marked as **Always Free-eligible** before creating it.
8. Save the assigned public IP address.

## 4. Open ports in OCI

In the subnet security list or Network Security Group associated with the instance, create ingress rules:

| Port | Source | Purpose |
| --- | --- | --- |
| 22 | Your public IP, ideally `/32` | SSH |
| 80 | `0.0.0.0/0` | HTTP redirection and TLS validation |
| 443 | `0.0.0.0/0` | Public HTTPS |

Do not expose PostgreSQL (`5432`) to the Internet.

## 5. Connect over SSH

From your local machine:

```bash
ssh ubuntu@<OCI_PUBLIC_IP>
```

Update the system:

```bash
sudo apt update
sudo apt upgrade -y
sudo reboot
```

Connect again over SSH after the reboot.

## 6. Install dependencies on the VM

Install Docker, the Compose plugin, Git, and basic utilities:

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg git ufw
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker ubuntu
```

Log out and reconnect so the `docker` group membership is applied:

```bash
exit
ssh ubuntu@<OCI_PUBLIC_IP>
```

Check Docker:

```bash
docker version
docker compose version
```

## 7. Configure the server firewall

Even when OCI network rules are configured, also restrict the local firewall:

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
sudo ufw status verbose
```

## 8. Create DNS records in Cloudflare

In Cloudflare DNS:

1. Create an `A` record:
   - Name: `api`
   - IPv4 address: `<OCI_PUBLIC_IP>`
   - Proxy status: **Proxied** or **DNS only**.
2. Create the Cloudflare Pages record later, after Pages provides the target.

For the initial setup, `DNS only` for `api.example.com` makes Caddy certificate issuance simpler. If you use Cloudflare's orange-cloud proxy, set SSL/TLS mode to **Full** or **Full (strict)** once the backend has a valid certificate.

## 9. Prepare backend variables

Create a deployment directory on the VM:

```bash
sudo mkdir -p /opt/budget-scope
sudo chown -R ubuntu:ubuntu /opt/budget-scope
cd /opt/budget-scope
```

Clone the repository:

```bash
git clone <REPOSITORY_URL> app
cd app
```

Create an environment file outside Git:

```bash
cat > /opt/budget-scope/api.env <<'EOF_ENV'
MICRONAUT_ENVIRONMENTS=prod
PORT=8080
DB_URL=jdbc:postgresql://postgres:5432/budget_scope
DB_USERNAME=budget_scope
DB_PASSWORD=<CHANGE_POSTGRES_PASSWORD>
OIDC_JWKS_URL=https://issuer.example.com/.well-known/jwks.json
CORS_ALLOWED_ORIGINS=https://app.example.com
EOF_ENV
chmod 600 /opt/budget-scope/api.env
```

Replace:

- `<CHANGE_POSTGRES_PASSWORD>` with a long password.
- `https://issuer.example.com/.well-known/jwks.json` with the real authentication provider when one exists.
- `https://app.example.com` with the real Cloudflare Pages domain.

## 10. Create the production Docker Compose file

Create `/opt/budget-scope/compose.prod.yml`:

```bash
cat > /opt/budget-scope/compose.prod.yml <<'EOF_COMPOSE'
services:
  postgres:
    image: postgres:18-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: budget_scope
      POSTGRES_USER: budget_scope
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U budget_scope -d budget_scope"]
      interval: 10s
      timeout: 5s
      retries: 5

  api:
    build:
      context: ./app
      dockerfile: infra/docker/api.Dockerfile
    restart: unless-stopped
    env_file:
      - /opt/budget-scope/api.env
    ports:
      - "127.0.0.1:8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres-data: {}
EOF_COMPOSE
```

Load `DB_PASSWORD` for Compose without committing it to the repository:

```bash
set -a
. /opt/budget-scope/api.env
set +a
docker compose -f /opt/budget-scope/compose.prod.yml up -d --build
```

Verify the services:

```bash
docker compose -f /opt/budget-scope/compose.prod.yml ps
curl -f http://127.0.0.1:8080/health
```

## 11. Install a reverse proxy with HTTPS

Caddy is the simplest option because it manages TLS automatically.

```bash
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https curl
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update
sudo apt install -y caddy
```

Create `/etc/caddy/Caddyfile`:

```bash
sudo tee /etc/caddy/Caddyfile > /dev/null <<'EOF_CADDY'
api.example.com {
  reverse_proxy 127.0.0.1:8080
}
EOF_CADDY
sudo systemctl reload caddy
```

Check it from your local machine or from the VM:

```bash
curl -f https://api.example.com/health
```

## 12. Back up PostgreSQL

Create the backup directory:

```bash
mkdir -p /opt/budget-scope/backups
chmod 700 /opt/budget-scope/backups
```

Create `/opt/budget-scope/backup-postgres.sh`:

```bash
cat > /opt/budget-scope/backup-postgres.sh <<'EOF_BACKUP'
#!/usr/bin/env bash
set -euo pipefail
BACKUP_DIR=/opt/budget-scope/backups
STAMP=$(date +%Y%m%d-%H%M%S)
docker compose -f /opt/budget-scope/compose.prod.yml exec -T postgres \
  pg_dump -U budget_scope -d budget_scope \
  | gzip > "${BACKUP_DIR}/budget_scope-${STAMP}.sql.gz"
find "${BACKUP_DIR}" -type f -name 'budget_scope-*.sql.gz' -mtime +14 -delete
EOF_BACKUP
chmod +x /opt/budget-scope/backup-postgres.sh
```

Schedule a daily cron job:

```bash
(crontab -l 2>/dev/null; echo "15 3 * * * /opt/budget-scope/backup-postgres.sh") | crontab -
```

For a prototype, this provides local backups. For important data, also copy backups to external storage.

## 13. Deploy the frontend on Cloudflare Pages

1. Open the **Cloudflare Dashboard**.
2. Go to **Workers & Pages > Create application > Pages**.
3. Connect the Git repository.
4. Configure the project:
   - Root directory: `apps/web`.
   - Framework preset: `Next.js`, if available.
   - Build command: `pnpm build`.
   - Output directory:
     - If Next.js is configured for static export: `out`.
     - If using Cloudflare's adapter for dynamic Next.js, follow the official Cloudflare guide for Next.js.
5. Add this environment variable in Cloudflare Pages:

```text
NEXT_PUBLIC_API_BASE_URL=https://api.example.com/api/v1
```

6. Run the first deployment.
7. Configure the custom domain `app.example.com` from the Pages custom domains section.

### Important note about Next.js

The current repository uses Next.js. There are two ways to deploy it to Cloudflare Pages:

1. **Static export**, recommended if the frontend only calls the API from the browser.
   - Add `output: "export"` in `apps/web/next.config.ts`.
   - Use `out` as the output directory.
2. **Dynamic Next.js on Cloudflare**, if server-side routes, server actions, or dynamic rendering are required.
   - Follow the official Cloudflare guide for Next.js with the current adapter.

For a low-cost prototype with a separate API, prefer static export.

## 14. Update backend CORS

After Cloudflare Pages assigns the final domain, update `/opt/budget-scope/api.env`:

```bash
nano /opt/budget-scope/api.env
```

Set:

```dotenv
CORS_ALLOWED_ORIGINS=https://app.example.com
```

Restart the API:

```bash
set -a
. /opt/budget-scope/api.env
set +a
docker compose -f /opt/budget-scope/compose.prod.yml up -d --build
```

## 15. Deployment flow for new versions

### Backend

On the VM:

```bash
cd /opt/budget-scope/app
git pull --ff-only
set -a
. /opt/budget-scope/api.env
set +a
docker compose -f /opt/budget-scope/compose.prod.yml up -d --build
curl -f https://api.example.com/health
```

### Frontend

1. Push to the branch connected to Cloudflare Pages.
2. Cloudflare Pages will run the build automatically.
3. Verify the production URL.

## 16. Final checklist

- [ ] The OCI VM is marked as Always Free-eligible.
- [ ] OCI ports are open: 22 restricted, 80 and 443 public.
- [ ] The local firewall allows 22, 80, and 443.
- [ ] PostgreSQL is not publicly exposed.
- [ ] `api.env` is not in Git and has `600` permissions.
- [ ] `DB_PASSWORD` is strong and unique.
- [ ] `CORS_ALLOWED_ORIGINS` points to the real Cloudflare Pages domain.
- [ ] `NEXT_PUBLIC_API_BASE_URL` points to the real API domain.
- [ ] `https://api.example.com/health` responds correctly.
- [ ] Cloudflare Pages deploys the frontend.
- [ ] A daily PostgreSQL backup exists.
