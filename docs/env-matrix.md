# CAP-48 — Environment Matrix

Settings that differ between Dev, Test and Prod. All values come from
`config/<env>.env` and are read by the backend at startup — nothing here is
hard-coded in the jar/image, and no row contains a secret value.

| Setting | Dev | Test | Prod |
|---|---|---|---|
| `APP_ENV` | `dev` | `test` | `prod` |
| `LOG_LEVEL` | `DEBUG` | `INFO` | `WARN` (never `DEBUG`) |
| `DB_URL` | `jdbc:postgresql://dev-db.cap48.internal:5432/app_dev` | `jdbc:postgresql://test-db.cap48.internal:5432/app_test` | `jdbc:postgresql://prod-db.cap48.internal:5432/app_prod` |
| `DB_USERNAME` | `app_dev_user` | `app_test_user` | `app_prod_user` |
| `DB_SECRET_REF` | `/dev/app/db-password` | `/test/app/db-password` | `/prod/app/db-password` |
| `SECRETS_PROVIDER` | `aws` | `aws` | `aws` |
| IAM role | `cap48-dev-app-role` | `cap48-test-app-role` | `cap48-prod-app-role` |

Notes:

- `DB_SECRET_REF` is a path into AWS Secrets Manager, never a password.
- `LOG_LEVEL=DEBUG` is allowed in Dev only; the config-validation script
  fails the pipeline if Prod (or Test) is set to `DEBUG`.
- Each environment's IAM role can read only its own secrets (see
  `docs/iam.md`, to be written alongside the access-audit feature).
- `APP_VERSION` is intentionally blank in these files — it is injected at
  deploy time with the single Docker image tag being promoted through
  Dev → Test → Prod (build once, promote).
