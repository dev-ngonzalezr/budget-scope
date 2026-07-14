# BudgetScope Engineering Instructions

This document defines the default engineering practices for BudgetScope. It is intended for maintainers, contributors, and coding agents working in the repository.

When a rule here conflicts with an accepted Architecture Decision Record, the ADR takes precedence. When implementation constraints require an exception, document the reason in the pull request.

## 1. Product objective

BudgetScope helps individuals and families understand their monthly financial commitments, track income and spending, and identify realistic opportunities to save.

Optimize the product for:

1. Correctness of financial calculations
2. Privacy and security
3. Clear household-level visibility
4. Explainable insights
5. Maintainability and self-hosting
6. A simple user experience

Do not optimize prematurely for massive scale, microservices, or complex event-driven infrastructure.

## 2. Architecture principles

### 2.1 Start with a modular monolith

The backend is one Micronaut deployable application divided into business modules. Each module should own its domain logic and persistence interfaces.

Recommended modules:

```text
identity
households
accounts
transactions
categories
recurringpayments
budgets
savings
insights
notifications
shared
```

A module may expose application services or domain events. It must not directly use another module's repository implementation or database entity as an internal shortcut.

### 2.2 Keep layers explicit

Use the following conceptual layers inside each module:

```text
api             HTTP controllers and transport DTOs
application     use cases and transaction boundaries
domain          business rules, value objects, and domain services
infrastructure  database, external systems, and framework adapters
```

Avoid placing business logic in controllers, persistence entities, or generated API clients.

### 2.3 Prefer synchronous flows initially

Use normal application-service calls and database transactions for core workflows. Introduce queues or asynchronous messaging only when there is a demonstrated requirement such as long-running imports, scheduled notifications, or unreliable external integrations.

### 2.4 Keep deployment portable

The primary hosted target is Google Cloud Run with Cloud SQL, but application code must not depend unnecessarily on Google-specific APIs.

Use interfaces around object storage, email, notification, and identity-provider integrations. Maintain a Docker Compose path for local development and self-hosting.

## 3. Technology baseline

Use the following baseline for new work:

| Area | Choice |
|---|---|
| Backend language | Java 25 LTS |
| Backend framework | Micronaut 5.0.x |
| Build | Gradle Wrapper with Kotlin DSL |
| HTTP | Micronaut HTTP Server Netty |
| Serialization | Micronaut Serialization Jackson |
| Validation | Jakarta Validation through Micronaut Validation |
| Persistence | Micronaut Data JDBC |
| Connection pool | HikariCP |
| Database | PostgreSQL |
| Migrations | Flyway |
| Authentication | OIDC/OAuth 2.0 with JWT validation |
| API contract | OpenAPI |
| Backend tests | JUnit 5 and Testcontainers |
| Web | Next.js, React, and TypeScript |
| Browser tests | Playwright |
| Android | Kotlin and Jetpack Compose, introduced later |
| Containers | Docker |
| CI/CD | GitHub Actions |
| Hosted target | Cloud Run and Cloud SQL |

Pin versions in build files and lockfiles. Use automated dependency updates, but merge updates only after CI passes and release notes have been reviewed for significant framework, database-driver, authentication, and migration changes.

## 4. Repository layout

Use this target structure:

```text
budget-scope/
├── apps/
│   ├── web/
│   └── android/
├── services/
│   └── api/
│       ├── src/main/java/
│       ├── src/main/resources/
│       │   ├── application.yml
│       │   └── db/migration/
│       └── src/test/java/
├── contracts/
│   └── openapi/
├── database/
│   └── seed/
├── infra/
│   ├── docker/
│   └── terraform/
├── docs/
│   ├── architecture/
│   └── adr/
├── .github/workflows/
├── docker-compose.yml
├── INSTRUCTIONS.md
├── LICENSE
└── README.md
```

Keep Flyway migrations under the API resources unless an infrastructure-independent reason emerges to manage them at the repository root.

## 5. Backend bootstrap

A new Micronaut service can be created through Micronaut Launch or the Micronaut CLI with features equivalent to:

```text
data-jdbc
flyway
jdbc-hikari
postgres
serialization-jackson
validation
security-jwt
security-oauth2
openapi
management
test-resources
testcontainers
```

Use Java and Gradle Kotlin DSL. Include the Gradle Wrapper in Git.

Do not add GraalVM Native Image to the critical path for the first release. Reassess it after measuring startup time, memory use, build complexity, and library compatibility in the deployed application.

## 6. Domain modelling rules

### 6.1 Money

Represent money as a dedicated immutable value object rather than passing raw `BigDecimal` values throughout the application.

A money value must include:

- Amount
- ISO 4217 currency code
- Defined scale and rounding behavior

Requirements:

- Use Java `BigDecimal`.
- Never use `double` or `float` for financial values.
- Construct `BigDecimal` from strings or integer minor units, not from floating-point values.
- Compare monetary values with domain-aware methods rather than relying on scale-sensitive `equals` where inappropriate.
- Define rounding at the use-case boundary.
- Validate that calculations do not combine incompatible currencies.

PostgreSQL monetary columns should use an explicit `NUMERIC(precision, scale)` selected for the supported use case. Do not use PostgreSQL's locale-dependent `money` type.

### 6.2 Dates and time

- Use `Instant` for system events and audit timestamps.
- Use `LocalDate` for due dates, statement dates, and budget periods that do not represent a moment in time.
- Store system timestamps in UTC.
- Store the user's or household's IANA time-zone identifier where local calendar behavior matters.
- Do not infer a time zone from a numeric UTC offset.

### 6.3 Identifiers

Use opaque identifiers. UUIDs are the default unless profiling demonstrates a compelling reason to choose another strategy.

Never expose sequential database identifiers where they make enumeration easier or couple clients to storage details.

### 6.4 Recurring payments

Model a recurring-payment rule separately from generated or matched transactions. A rule should be able to describe:

- Expected amount or amount range
- Currency
- Frequency
- Start and optional end date
- Expected day or recurrence expression
- Merchant or payee
- Category
- Account
- Reminder settings
- Active, paused, or ended status

Do not silently create permanent rules from imported data. Suggestions must be explainable and confirmed by the user.

### 6.5 Household isolation

Every household-owned record must carry or derive a household identifier. Authorization checks must happen server-side for every operation.

Never trust a client-provided household ID without verifying that the authenticated principal has access to that household and the requested action.

## 7. API design

### 7.1 General conventions

- Base path: `/api/v1`
- JSON request and response bodies
- OpenAPI generated during the build
- Consistent error representation, preferably RFC 9457 Problem Details
- Cursor pagination for potentially large or continuously changing collections
- Idempotency support for imports and other retry-prone write operations

Use nouns for resources and HTTP semantics consistently.

```text
GET    /api/v1/households/{householdId}/transactions
POST   /api/v1/households/{householdId}/transactions
GET    /api/v1/households/{householdId}/recurring-payments
PATCH  /api/v1/households/{householdId}/budgets/{budgetId}
```

### 7.2 DTO boundaries

Do not return persistence entities from controllers. Define transport DTOs and map them at the application boundary.

Generated web and Android clients should consume the published OpenAPI contract. Do not maintain handwritten duplicate API interfaces in each client.

### 7.3 Compatibility

Prefer additive changes:

- Add optional fields instead of changing existing meaning.
- Do not reuse an enum value for a different meaning.
- Deprecate before removing.
- Add a new API version when a breaking change cannot be avoided.

CI should detect unexpected OpenAPI contract changes.

## 8. Persistence and migrations

### 8.1 Micronaut Data JDBC

Use repositories for straightforward persistence. Keep queries explicit and avoid hiding important financial behavior in database callbacks.

Use jOOQ selectively if reporting queries become too complex for clear repository methods. Do not introduce a second persistence approach without an ADR.

### 8.2 Transactions

Application services define transaction boundaries. A use case that changes multiple financial records atomically must execute in one database transaction.

External network calls should generally not occur while a database transaction is held open.

### 8.3 Flyway

- Every schema change requires a migration.
- Never edit a migration that may already have run outside a developer's disposable local database.
- Use forward-only migrations by default.
- Give migrations descriptive names.
- Test migrations from an empty database and from the latest released schema.
- Treat destructive changes as multi-step deployments when necessary.

Example:

```text
V001__create_households.sql
V002__create_accounts.sql
V003__create_transactions.sql
```

Do not use automatic schema generation in shared, staging, or production environments.

## 9. Authentication and authorization

Use an external OIDC provider. The API validates access tokens and maps the external subject to an internal user identity.

Requirements:

- Validate issuer, audience, signature, and token expiry.
- Use short-lived access tokens.
- Do not store raw access or refresh tokens in logs or database audit records.
- Perform resource authorization in application services or dedicated policy components.
- Test cross-household access denial explicitly.
- Apply least privilege to service accounts and deployment identities.

Authentication proves identity; it does not grant access to every household resource.

## 10. Configuration and secrets

Configuration priority should be:

1. Explicit environment variables
2. Environment-specific Micronaut configuration
3. Safe committed defaults

Provide `.env.example` files, but do not load production secrets from repository files.

Suggested backend variables:

```dotenv
MICRONAUT_ENVIRONMENTS=local
DB_URL=jdbc:postgresql://localhost:5432/budget_scope
DB_USERNAME=budget_scope
DB_PASSWORD=budget_scope
OIDC_ISSUER_URL=https://issuer.example.com/
OIDC_AUDIENCE=budget-scope-api
CORS_ALLOWED_ORIGINS=http://localhost:3000
OBJECT_STORAGE_BUCKET=
OTEL_EXPORTER_OTLP_ENDPOINT=
```

Production secrets belong in Google Secret Manager or the equivalent facility of the selected host.

## 11. Java coding standards

- Prefer immutable objects.
- Use Java records for simple immutable data carriers when appropriate.
- Keep methods focused and name them after business intent.
- Prefer constructor injection.
- Avoid field injection.
- Avoid static mutable state.
- Avoid framework annotations in the domain layer when practical.
- Validate at system boundaries and enforce invariants inside the domain model.
- Use checked or domain-specific exceptions only where callers can act on them.
- Convert internal exceptions to stable API errors at the HTTP boundary.
- Do not catch broad `Exception` unless rethrowing after adding meaningful context or performing required cleanup.

Use an automated formatter and static analysis in CI. The selected tools must run identically locally and in GitHub Actions.

## 12. Web application standards

- Use TypeScript in strict mode.
- Treat the generated OpenAPI client as the API integration boundary.
- Keep server state separate from local UI state.
- Make dashboards accessible by keyboard and screen readers.
- Do not encode financial meaning only through color.
- Format currency and dates with locale-aware standard APIs.
- Avoid exposing secrets in variables available to browser code.
- Prefer responsive layouts before introducing platform-specific clients.

The first release should work well on mobile browsers and be installable as a PWA where practical.

## 13. Android standards

The Android application is a later milestone. When introduced:

- Use Kotlin and Jetpack Compose.
- Follow unidirectional data flow.
- Separate UI, domain, and data concerns.
- Use the generated Kotlin API client.
- Store credentials using Android security APIs.
- Design offline behavior explicitly; do not pretend writes succeeded before conflict behavior is defined.
- Keep Android-specific business rules to a minimum.

The backend remains the authority for household authorization and final financial state.

## 14. Testing strategy

### 14.1 Unit tests

Unit-test domain rules without starting Micronaut or PostgreSQL whenever possible.

Prioritize tests for:

- Money arithmetic and rounding
- Budget calculations
- Recurrence generation
- Date-boundary behavior
- Household authorization policies
- Savings insight rules
- Import parsing and deduplication

### 14.2 Integration tests

Use Testcontainers with PostgreSQL for repository, migration, and API integration tests. Avoid relying on H2 as a substitute for PostgreSQL behavior.

Integration tests should verify:

- Flyway migrations
- Database constraints
- Repository queries
- Transaction behavior
- Authentication and authorization
- JSON serialization
- OpenAPI conformance

### 14.3 End-to-end tests

Use Playwright for a small set of critical user journeys:

1. Create or join a household
2. Add an account
3. Record income and expenses
4. Add a recurring payment
5. Review the monthly dashboard
6. Create a savings goal

Do not make end-to-end tests responsible for every business-rule edge case.

### 14.4 Test data

Use synthetic financial data only. Never copy real bank statements, access tokens, personal identifiers, or production records into fixtures or test logs.

## 15. Git and pull-request workflow

Use short-lived branches and pull requests.

Suggested branch naming:

```text
feature/recurring-payment-calendar
fix/monthly-total-rounding
docs/api-error-format
chore/upgrade-micronaut
```

Commits should be focused and explain the reason for the change. Conventional Commits may be adopted if release automation uses them consistently.

Every pull request should include:

- Problem or user need
- Approach taken
- Security and privacy impact
- Database or API compatibility impact
- Tests added or updated
- Screenshots for visible UI changes
- Follow-up work that was deliberately excluded

Do not merge while required checks are failing.

## 16. GitHub Actions

Keep workflows small and purpose-specific.

### 16.1 Pull-request workflow

Backend jobs:

```text
checkout
set up JDK 25
set up and cache Gradle
verify Gradle Wrapper
run formatting/static analysis
run ./gradlew check
validate Flyway migrations
build the application
build the container image without publishing
```

Web jobs:

```text
checkout
set up Node Active LTS
set up pnpm and cache dependencies
install with frozen lockfile
lint
type-check
unit test
production build
```

Cross-project jobs:

```text
generate and compare OpenAPI contract
secret scanning
dependency review
container vulnerability scan
```

### 16.2 Main-branch delivery

On a successful merge to `main`:

1. Run all required tests.
2. Build immutable images.
3. Tag images with the Git commit SHA.
4. Push images to Artifact Registry or GHCR.
5. Run migrations as a controlled one-off job.
6. Deploy to the development or staging environment.
7. Run smoke tests.

### 16.3 Production release

Production should promote a previously tested image digest. Do not rebuild source during promotion.

Use GitHub environments for protected production deployment and OIDC/Workload Identity Federation instead of long-lived cloud service-account keys.

## 17. Deployment

Recommended hosted architecture:

```text
Next.js web             Cloud Run service
Micronaut API           Cloud Run service
PostgreSQL              Cloud SQL
Migrations/jobs         Cloud Run Job
Attachments/exports     Cloud Storage
Secrets                 Secret Manager
Images                   Artifact Registry
Metrics/traces/logs     OpenTelemetry-compatible backend
```

Deployment requirements:

- Containers listen on the platform-provided port.
- Services are stateless between requests.
- Database connections are pooled conservatively to respect Cloud SQL limits.
- Health endpoints distinguish liveness from readiness.
- Migrations are not run concurrently by every application instance.
- Rollback procedures are documented.
- Backups and restore tests exist before storing production data.

Avoid Kubernetes until operational requirements clearly justify it.

## 18. Observability

Use structured logs and correlation identifiers.

Capture:

- Request rate, latency, and error rate
- Database pool usage and query latency
- Failed authentication and authorization events without sensitive token data
- Import success and failure counts
- Background-job duration and failures
- Notification delivery status

Never log:

- Access or refresh tokens
- Full imported transaction descriptions by default
- Bank account numbers
- Personally identifying financial exports
- Raw request bodies for sensitive endpoints

Use OpenTelemetry-compatible metrics and traces where practical. Sampling and retention must respect privacy and cost constraints.

## 19. Security and privacy checklist

Before production:

- Perform threat modelling for household isolation and account takeover.
- Enable dependency, secret, and container scanning.
- Protect default branches and release environments.
- Use least-privileged database and cloud identities.
- Define data retention and deletion behavior.
- Encrypt transport and managed storage.
- Rate-limit authentication-adjacent and import endpoints.
- Validate uploaded file type and size.
- Protect CSV exports against spreadsheet formula injection.
- Document incident response and credential rotation.
- Test backup restoration.
- Review privacy and regulatory obligations for target regions.

Do not claim compliance with a regulation unless it has been properly assessed and documented.

## 20. Insights and automation rules

Savings insights must be explainable. Each recommendation should identify the underlying data and the calculation used.

Examples:

- A subscription increased by a defined percentage.
- A recurring payment has not been used or confirmed recently.
- Spending in a category exceeded a user-defined budget.
- A lower monthly savings target is more achievable given committed expenses.

Do not present probabilistic output as fact. Label estimates and forecasts clearly. Never initiate transfers, cancel services, or change financial records without explicit user action.

## 21. Dependency management

- Commit Gradle and package-manager lockfiles where supported.
- Use the Gradle Wrapper and verify its integrity in CI.
- Use a frozen pnpm lockfile in CI.
- Configure automated dependency update pull requests.
- Group low-risk development dependency updates where useful.
- Review authentication, database driver, serialization, migration, and framework updates individually.
- Patch security issues promptly.
- Do not automatically deploy dependency updates to production without tests and review.

## 22. Architecture Decision Records

Create an ADR for decisions that are difficult to reverse or affect multiple modules.

Examples:

- Identity provider selection
- API versioning strategy
- Multi-currency accounting model
- Event or queue adoption
- Object storage provider
- Reporting persistence technology
- Native mobile architecture

Store ADRs under `docs/adr/` using sequential filenames:

```text
0001-use-modular-monolith.md
0002-use-postgresql.md
0003-use-external-oidc-provider.md
```

Each ADR should describe context, decision, alternatives, consequences, and status.

## 23. Definition of done

A change is done when:

- Acceptance criteria are met.
- Domain and authorization rules are covered by tests.
- Relevant integration tests pass against PostgreSQL.
- API documentation is updated.
- Database migrations are included and tested when required.
- Logging does not expose sensitive data.
- Accessibility has been considered for UI changes.
- CI passes.
- Deployment and rollback implications are understood.
- User-facing documentation is updated when behavior changes.

## 24. Initial implementation sequence

Recommended order:

1. Repository, build, formatting, and CI foundations
2. Local Docker Compose PostgreSQL environment
3. Micronaut API skeleton, health checks, and OpenAPI generation
4. OIDC integration and internal user mapping
5. Household membership and authorization model
6. Accounts, categories, and transactions
7. Recurring payments and monthly calendar
8. Budgets and cash-flow dashboard
9. Savings goals and explainable insights
10. CSV import/export
11. Staging deployment and operational controls
12. Responsive web polish and PWA behavior
13. Android discovery and implementation

Build vertical slices that are usable end to end rather than implementing every backend entity before beginning the web interface.

## 25. Useful commands

From `services/api`:

```bash
./gradlew clean
./gradlew check
./gradlew test
./gradlew run
./gradlew assemble
./gradlew dockerBuild
./gradlew dependencyUpdates
```

Some tasks require plugins and may not exist until configured. Prefer documented Gradle tasks over ad hoc shell scripts.

From `apps/web`:

```bash
pnpm install
pnpm dev
pnpm lint
pnpm typecheck
pnpm test
pnpm build
```

From the repository root:

```bash
docker compose up -d
docker compose logs -f
docker compose down
```

## 26. External references

- Micronaut 5.0.4 documentation: https://docs.micronaut.io/5.0.4/guide/
- Micronaut Data JDBC guides: https://guides.micronaut.io/latest/tag-data_jdbc.html
- Micronaut PostgreSQL guides: https://guides.micronaut.io/latest/tag-postgres.html
- Micronaut Security: https://micronaut-projects.github.io/micronaut-security/latest/guide/
- Java support roadmap: https://www.oracle.com/java/technologies/java-se-support-roadmap.html
- PostgreSQL documentation: https://www.postgresql.org/docs/
- Flyway documentation: https://documentation.red-gate.com/fd
- Testcontainers for Java: https://java.testcontainers.org/
- OpenAPI specification: https://spec.openapis.org/oas/latest.html
- GitHub Actions Gradle guide: https://docs.github.com/actions/automating-builds-and-tests/building-and-testing-java-with-gradle
- Cloud Run documentation: https://cloud.google.com/run/docs
- Cloud SQL PostgreSQL documentation: https://cloud.google.com/sql/docs/postgres
