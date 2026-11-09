# CAP-48 — Database Schema

One small Postgres database records every check, rotation and deployment so
the project has real data to validate, reconcile and compute KPIs from.
**It never stores a secret value — only fingerprints and references.**

## Where the database lives

| Environment | Database |
|---|---|
| Local (your laptop) | Postgres in Docker, fake password from a git-ignored file |
| Dev / Test / Prod (AWS) | One small AWS RDS Postgres instance, with three separate databases/users: `app_dev`, `app_test`, `app_prod` — each environment can reach only its own |

## Tables

### `environments`
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| name | text | `dev` / `test` / `prod` |
| created_at | timestamp | |

### `config_snapshots`
Stores a point-in-time copy of an environment's **non-secret** settings.
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| environment_id | FK → environments | |
| captured_at | timestamp | |
| settings_json | jsonb | key/value pairs, e.g. `{"LOG_LEVEL":"WARN","DB_HOST":"prod-db"}` — never a password |

### `validation_results`
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| environment_id | FK | |
| checked_at | timestamp | |
| status | text | `PASS` / `FAIL` |
| errors_json | jsonb | list of missing/invalid settings, if any |

### `drift_findings`
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| compared_at | timestamp | |
| env_a_id | FK → environments | |
| env_b_id | FK → environments | |
| setting_key | text | e.g. `LOG_LEVEL` |
| value_a | text | |
| value_b | text | |
| allowed | boolean | true if this key is on the "allowed to differ" list |

### `scan_findings`
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| scanned_at | timestamp | |
| file_path | text | e.g. `backend/Foo.java` |
| line_number | int | |
| rule_matched | text | which pattern matched (e.g. "hardcoded password") |
| commit_sha | text | git commit being scanned |
| **(no column ever holds the matched secret value)** | | |

### `rotation_events`
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| environment_id | FK | |
| started_at | timestamp | |
| overlap_ends_at | timestamp | |
| completed_at | timestamp, nullable | |
| old_fingerprint | text(8) | |
| new_fingerprint | text(8) | |
| status | text | `in_progress` / `switched` / `rolled_back` / `failed` |

### `deployments`
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| environment_id | FK | |
| image_version | text | the single promoted build's tag/SHA |
| deployed_at | timestamp | |
| verified | boolean | result of the deployment-verification script |
| verification_details_json | jsonb | health status, env name match, fingerprint match |

### `access_audit`
| Column | Type | Notes |
|---|---|---|
| id | serial PK | |
| environment_id | FK | |
| iam_role | text | e.g. `cap48-prod-app-role` |
| permission | text | e.g. `secretsmanager:GetSecretValue` on `/prod/app/*` |
| justification | text | one-line reason |

## KPIs computed from this schema

| KPI | Query source |
|---|---|
| Drift count | `count(*) from drift_findings where allowed = false and compared_at = <latest run>` |
| Secret findings | `count(*) from scan_findings where commit_sha = <latest>` |
| Secret age (days) | `now() - completed_at` from the latest `rotation_events` row per environment |
| Rotation success rate | `count(status='switched') / count(*)` from `rotation_events` |
| Deployment verification rate | `count(verified=true) / count(*)` from `deployments` |

## Migration tool

Use Flyway (works cleanly with Spring Boot). SQL migration files live in
`backend/src/main/resources/db/migration/V1__init.sql`, etc. Running
migrations is part of `configuration validation` — if a migration fails,
the pipeline stops before deployment.
