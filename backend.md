# CAP-48 — Backend (Spring Boot)

How the Spring Boot backend is structured, what each part does, and the rules
it must follow. Read this before writing any backend code.

## Tech

- Java 17, Spring Boot 3.x
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-actuator`,
  `spring-boot-starter-data-jpa`, `postgresql` driver, `software.amazon.awssdk:secretsmanager`
- Build tool: Maven

## Package layout

```
backend/
  src/main/java/com/cap48/
    Cap48Application.java
    config/
      AppProperties.java       # binds app.* settings from application.yml
    secrets/
      SecretProvider.java      # interface: get(), fingerprint()
      FileSecretProvider.java  # reads secret from a local file (local dev)
      AwsSecretProvider.java   # reads secret from AWS Secrets Manager (dev/test/prod)
    web/
      InfoController.java      # GET /api/config-info
      HealthIndicator.java     # extra Actuator health check: DB reachable?
    db/
      (JPA entities + repositories for the schema in dbschema.md)
```

## Configuration (externalized)

`application.yml` reads everything from environment variables — nothing is
hard-coded:

```yaml
app:
  env-name: ${APP_ENV:local}
  version: ${APP_VERSION:dev-local}
  log-level: ${LOG_LEVEL:INFO}
db:
  url: ${DB_URL}
  username: ${DB_USERNAME}
  secret-ref: ${DB_SECRET_REF}        # e.g. "/prod/app/db-password" (AWS) or
                                        # a file path (local dev) — never a password
secrets:
  provider: ${SECRETS_PROVIDER:file}   # "file" locally, "aws" in dev/test/prod
```

## Secret handling rule (non-negotiable)

- `SecretProvider.get()` returns the raw password **only** to the code that
  opens the DB connection. It is never logged, never returned from an HTTP
  endpoint, never put in a response body.
- `SecretProvider.fingerprint()` returns `sha256(secret).substring(0, 8)` —
  safe to log, store in the DB, and show in the UI.
- Every log statement that could touch a secret must log the fingerprint,
  never the value. Code review / self-review must check this before every
  commit.

## Endpoints

| Method | Path | Returns | Purpose |
|---|---|---|---|
| GET | `/actuator/health` | `{"status":"UP"}` | Deployment verification |
| GET | `/api/config-info` | `{"env":"prod","version":"a1b2c3","secretFingerprint":"e5f6a7b8"}` | Proves which env + secret version is running, without exposing the secret |
| GET | `/api/kpis` | Summary counts from the DB (drift count, scan findings, rotation success rate, etc.) | Feeds the React dashboard and the data/KPI write-up |

## Error handling

- Missing required env var at startup → fail fast with a clear message
  naming the missing variable (never a secret value).
- Secret fetch fails (AWS unreachable, path wrong) → `/actuator/health`
  reports `DOWN` with reason `"secret unavailable"`, no stack trace with
  secret content.
- DB connection fails after a rotation → the app keeps using its last-known
  good connection pool until the overlap window ends (rollback path), not a
  hard crash.

## Local development

- `docker-compose.yml` (local only, not deployed) starts a Postgres
  container with a **fake** password stored in `secrets/local-db.secret`
  (git-ignored).
- `SECRETS_PROVIDER=file` and `DB_SECRET_REF=./secrets/local-db.secret` in
  a local `.env` file (also git-ignored).
- Developers never need AWS credentials to run the app locally.

## What NOT to do

- Do not print `db.password` or any `SecretProvider.get()` result via
  `System.out`, `log.info`, exception messages, or an HTTP response.
- Do not cache a secret value in a static field longer than the DB
  connection needs it.
- Do not add a debug/test endpoint that echoes config or environment
  variables wholesale (it could leak `DB_SECRET_REF` paths, which is lower
  risk than a password but should still be avoided).
