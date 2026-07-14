# BudgetScope

BudgetScope is an open-source personal and family finance platform for understanding recurring payments, monthly spending, household cash flow, and potential savings opportunities.

The project is designed to answer practical questions such as:

- What payments are due this month?
- How much does the household spend by category?
- Which expenses are recurring or increasing?
- How much disposable income remains after committed payments?
- Where could the household reduce costs or improve its savings rate?

## Project status

BudgetScope is in its initial architecture and implementation phase. The first milestone is a responsive web application backed by a modular Micronaut API and PostgreSQL database. A native Android application may be added after the core API and workflows are stable.

## Planned capabilities

### Initial release

- User authentication and household membership
- Personal and shared financial accounts
- Income and expense transactions
- Categories and configurable tags
- Recurring payments and expected-payment calendar
- Monthly budget planning
- Cash-flow dashboard
- Savings goals
- CSV import and export
- Basic spending and savings insights

### Later releases

- Native Android application
- Push notifications and payment reminders
- Receipt attachments and document storage
- Bank-data integrations where legally and technically appropriate
- Advanced forecasting and anomaly detection
- Multi-currency support
- Configurable household roles and approval workflows

## Architecture

BudgetScope starts as a **modular monolith**. This keeps deployment and local development simple while preserving clear boundaries between business domains.

```text
Web application ─┐
                 ├── REST API ── PostgreSQL
Android app ─────┘       │
                         ├── Background jobs
                         └── Object storage
```

The backend should be separated into domain-oriented modules such as:

- identity
- households
- accounts
- transactions
- categories
- recurring payments
- budgets
- savings goals
- insights
- notifications

Following clean architecture, modules may communicate inside one deployable application, but they should not access each other's persistence implementation directly.

## Technology stack

### Backend

- Java 25 LTS
- Micronaut Framework 5.0.x
- Gradle with Kotlin DSL and the Gradle Wrapper
- Micronaut Data JDBC
- PostgreSQL
- Flyway database migrations
- Micronaut Security with OIDC/JWT
- OpenAPI
- JUnit 5
- Testcontainers

### Web

- Next.js
- React
- TypeScript
- An OpenAPI-generated API client
- A component library selected during implementation
- Playwright for browser-level tests

### Android

Planned after the first web release:

- Kotlin
- Jetpack Compose
- An OpenAPI-generated Kotlin client
- Android Keystore-backed credential storage

### Platform and delivery

- GitHub Actions
- Docker
- Google Cloud Run
- Google Cloud SQL for PostgreSQL
- Google Cloud Storage for attachments and exports
- Google Secret Manager
- Google Artifact Registry
- OpenID Connect / Workload Identity Federation for CI deployment

The deployment platform is intentionally replaceable. The application should also remain runnable through Docker Compose for local development and self-hosting.

## Proposed repository structure

```text
budget-scope/
├── apps/
│   ├── web/                    # Next.js application
│   └── android/                # Kotlin/Compose application, added later
├── services/
│   └── api/                    # Micronaut backend
├── contracts/
│   └── openapi/                # Published API contract and generated artifacts
├── database/
│   ├── migrations/             # Flyway migrations if managed at repository level
│   └── seed/                   # Local development seed data
├── infra/
│   ├── docker/
│   └── terraform/              # Optional infrastructure as code
├── .github/
│   └── workflows/
├── docker-compose.yml
├── INSTRUCTIONS.md
├── LICENSE
└── README.md
```

## Local development

### Requirements

- Git
- JDK 25
- Docker with Docker Compose
- Node.js Active LTS
- pnpm

The repository should use the included Gradle Wrapper. A global Gradle installation is not required.

### Start local infrastructure

```bash
docker compose up -d postgres
```

### Run the backend

```bash
cd services/api
./gradlew run
```

On Windows:

```powershell
cd services/api
.\gradlew.bat run
```

### Run the web application

```bash
cd apps/web
pnpm install
pnpm dev
```

### Run backend tests

```bash
cd services/api
./gradlew check
```

### Run web checks

```bash
cd apps/web
pnpm lint
pnpm typecheck
pnpm test
pnpm build
```

Exact scripts may change while the repository is being bootstrapped. `INSTRUCTIONS.md` is the source of truth for engineering conventions.

## Configuration

Applications must read configuration from environment variables or local development files that are excluded from Git. Never commit secrets.

Expected backend variables include:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/budget_scope
DB_USERNAME=budget_scope
DB_PASSWORD=budget_scope
OIDC_ISSUER_URL=https://example-issuer/
OIDC_CLIENT_ID=budget-scope-api
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

A committed `.env.example` should document every supported variable without containing real credentials.

## Financial data principles

- Monetary values must never use binary floating-point types.
- Store an amount together with its ISO 4217 currency code.
- Use fixed-precision database columns and Java `BigDecimal`.
- Define and test rounding rules explicitly.
- Store timestamps in UTC and convert them for presentation.
- Represent calendar dates that have no time component as dates, not timestamps.
- Keep an audit trail for important financial changes.
- Do not log access tokens, account identifiers, imported statements, or other sensitive financial data.

## CI/CD

Pull requests should run:

- Backend compilation, formatting, static analysis, unit tests, integration tests, and packaging
- Web linting, type checking, tests, and production build
- OpenAPI compatibility checks
- Database migration validation
- Container image build validation
- Dependency and secret scanning

Merges to `main` should build immutable container images tagged with the Git commit SHA and deploy them to a non-production environment. Production releases should promote previously tested artifacts rather than rebuilding them.

## API approach

BudgetScope uses versioned REST endpoints and OpenAPI documentation. Generated clients should be used by the web and Android applications.

Example base path:

```text
/api/v1
```

API changes should be additive whenever possible. Breaking changes require a documented migration path and a new API version when compatibility cannot be preserved.

## Security and privacy

BudgetScope handles sensitive financial information. Security and privacy are product requirements, not optional enhancements.

At minimum:

- Use OIDC/OAuth 2.0 rather than implementing password storage from scratch
- Enforce household-level authorization on every protected operation
- Encrypt traffic in transit
- Encrypt managed storage at rest
- Keep secrets outside the source repository
- Minimize retained personal data
- Support account and household data deletion
- Redact sensitive fields from logs and traces
- Review dependencies and container images continuously
- Document backup and restore procedures before production use

## License

The intended license is **Apache License 2.0**, subject to confirmation before the first public release. It permits broad use and contribution and includes an explicit patent grant.

## Documentation

Engineering and contribution rules are documented in [`INSTRUCTIONS.md`](./INSTRUCTIONS.md).

## Reference documentation

- Micronaut: https://docs.micronaut.io/5.0.4/guide/
- Micronaut Guides: https://guides.micronaut.io/latest/
- Java release roadmap: https://www.oracle.com/java/technologies/java-se-support-roadmap.html
- GitHub Actions for Gradle: https://docs.github.com/actions/automating-builds-and-tests/building-and-testing-java-with-gradle
- Cloud Run: https://cloud.google.com/run/docs
- Cloud SQL for PostgreSQL: https://cloud.google.com/sql/docs/postgres
