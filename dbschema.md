# CAP-48 — Database Schema

One small **MySQL** database records every check, rotation and deployment so
the project has real data to validate, reconcile and compute KPIs from.
**It never stores a secret value — only fingerprints and references.**

The 8 tables below all live in **one shared database, `cap48_audit`**, not
in each environment's own `app_dev`/`app_test`/`app_prod` database. Dev,
Test and Prod each run their own app instance, but all three connect to
this same shared database/user — that's what lets the Compare and History
pages see all three environments at once. Each environment's own
`app_dev`/`app_test`/`app_prod` database stays isolated per environment
(nothing else uses it yet) and is configured completely separately
(`DB_URL`/`DB_USERNAME`/`DB_SECRET_REF` vs. `AUDIT_DB_URL`/
`AUDIT_DB_USERNAME`/`AUDIT_DB_SECRET_REF` in `config/<env>.env`).

## Who writes to it

Nothing seeds these tables ahead of time — `environments` and the other 7
tables are populated only by 6 write (POST) endpoints
(`/api/scan-findings`, `/api/validation-results`, `/api/drift-findings`,
`/api/config-snapshots`, `/api/deployments`, `/api/rotate/write-api-key/{env}`),
called by the pipeline's `scan_secrets.py`, `validate_config.py`, `drift.py`,
the deploy/verify steps, and (for rotation) a person clicking "Rotate now"
in the dashboard. The 5 pipeline-driven writes are authenticated by an
`X-Write-Api-Key` header checked against that environment's own
`WRITE_API_KEY_SECRET_REF` secret — one key per environment, same pattern
as `DB_SECRET_REF`, not shared like the audit DB password. The rotation
endpoint is authenticated differently, deliberately: HTTP Basic Auth
against `ROTATE_AUTH_USERNAME`/`ROTATE_AUTH_PASSWORD_SECRET_REF` (also
per-environment; `ROTATE_AUTH_USERNAME` is the one setting expected to be
identical everywhere). Rotating the write-API key can't require
presenting that same key as the credential from an unauthenticated
browser, so it uses its own human credential instead — the browser's
native Basic Auth prompt, not anything embedded in frontend code. The
read-only `GET /api/...` endpoints the frontend calls are not behind
either credential.

`rotation_events` now has its first writer: `POST /api/rotate/write-api-key/{env}`
generates a new random token, calls Secrets Manager's `PutSecretValue` on
that environment's own `write-api-key` secret, updates this instance's
in-memory cached key immediately, and records one row (old/new
fingerprint, never the raw value, plus `switched`/`failed` status). `{env}`
must match the instance's own `APP_ENV` — each app role only has IAM
permission on its own write-api-key ARN, so cross-environment rotation
isn't just blocked in code, it would fail in AWS anyway. This is phase one
of rotation and only covers the write-API key, not the DB or audit-DB
passwords.

## Where the database lives

| Environment | Database |
|---|---|
| Local (your laptop) | MySQL in Docker, fake password from a git-ignored file. Inspect it with **MySQL Workbench** connected to `localhost:3306` only. |
| Dev / Test / Prod (AWS), own per-env database | One small AWS RDS MySQL instance, with three separate databases/users: `app_dev`, `app_test`, `app_prod` — each environment can reach only its own. Nothing uses this yet. |
| Dev / Test / Prod (AWS), shared audit database | The same RDS instance, one shared database `cap48_audit` with its own user (`cap48_audit_user`), reachable from all three environments — this is where the 8 tables below actually live |

## Tables

(MySQL types: `serial PK` → `INT AUTO_INCREMENT PRIMARY KEY`, `jsonb` → `JSON`)

### `environments`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| name | VARCHAR(20) | `dev` / `test` / `prod` |
| created_at | DATETIME | |

### `config_snapshots`
Stores a point-in-time copy of an environment's **non-secret** settings.
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| environment_id | INT, FK → environments | |
| captured_at | DATETIME | |
| settings_json | JSON | key/value pairs, e.g. `{"LOG_LEVEL":"WARN","DB_HOST":"prod-db"}` — never a password |

### `validation_results`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| environment_id | INT, FK | |
| checked_at | DATETIME | |
| status | VARCHAR(10) | `PASS` / `FAIL` |
| errors_json | JSON | list of missing/invalid settings, if any |
| commit_sha | VARCHAR(64), nullable | git commit this check ran against, for the History page join; null on older rows |

### `drift_findings`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| compared_at | DATETIME | |
| env_a_id | INT, FK → environments | |
| env_b_id | INT, FK → environments | |
| setting_key | VARCHAR(100) | e.g. `LOG_LEVEL` |
| value_a | VARCHAR(255) | |
| value_b | VARCHAR(255) | |
| allowed | BOOLEAN | true if this key is on the "allowed to differ" list |
| commit_sha | VARCHAR(64), nullable | git commit this comparison ran against, for the History page join; null on older rows |

### `scan_findings`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| scanned_at | DATETIME | |
| file_path | VARCHAR(500) | e.g. `backend/Foo.java` |
| line_number | INT | |
| rule_matched | VARCHAR(100) | which pattern matched (e.g. "hardcoded password") |
| commit_sha | VARCHAR(40) | git commit being scanned |
| **(no column ever holds the matched secret value)** | | |

### `rotation_events`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| environment_id | INT, FK | |
| started_at | DATETIME | |
| overlap_ends_at | DATETIME | |
| completed_at | DATETIME, nullable | |
| old_fingerprint | CHAR(8) | |
| new_fingerprint | CHAR(8) | |
| status | VARCHAR(20) | `in_progress` / `switched` / `rolled_back` / `failed` |
| commit_sha | VARCHAR(64), nullable | git commit this rotation happened as part of, for the History page join; null on older rows |

### `deployments`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| environment_id | INT, FK | |
| image_version | VARCHAR(100) | the single promoted build's tag/SHA |
| deployed_at | DATETIME | |
| verified | BOOLEAN | result of the deployment-verification script |
| verification_details_json | JSON | health status, env name match, fingerprint match |

### `access_audit`
| Column | Type | Notes |
|---|---|---|
| id | INT AUTO_INCREMENT PK | |
| environment_id | INT, FK | |
| iam_role | VARCHAR(100) | e.g. `cap48-prod-app-role` |
| permission | VARCHAR(255) | e.g. `secretsmanager:GetSecretValue` on `/prod/app/*` |
| justification | VARCHAR(500) | one-line reason |
| captured_at | DATETIME, nullable | when this audit row was recorded, for ordering; null on older rows |

## KPIs computed from this schema

| KPI | Query source |
|---|---|
| Drift count | `SELECT COUNT(*) FROM drift_findings WHERE allowed = FALSE AND compared_at = <latest run>` |
| Secret findings | `SELECT COUNT(*) FROM scan_findings WHERE commit_sha = <latest>` |
| Secret age (days) | `DATEDIFF(NOW(), completed_at)` from the latest `rotation_events` row per environment |
| Rotation success rate | `SUM(status='switched') / COUNT(*)` from `rotation_events` |
| Deployment verification rate | `SUM(verified=TRUE) / COUNT(*)` from `deployments` |

## Migration tool

Use Flyway (works cleanly with Spring Boot and MySQL). SQL migration files
live in `backend/src/main/resources/db/migration/V1__init.sql`, etc.
Running migrations is part of `configuration validation` — if a migration
fails, the pipeline stops before deployment.
