# CAP-48 — Features

This file lists every feature the app must have, grouped as mandatory (required by
the project brief) and extra (nice to have if time allows). Build mandatory features
first, in the order listed in `README` / the build plan.

## Mandatory features

1. **Environment matrix**
   A table (in `docs/env-matrix.md`) showing how Dev, Test and Prod differ:
   log level, database host, secret path, etc.

2. **Externalized configuration**
   Settings live outside the app, in one file per environment
   (`config/dev.env`, `config/test.env`, `config/prod.env`). The app reads them
   at startup. No settings are hard-coded in the jar/image.

3. **Secret reference handling**
   Config files store only *where* a secret lives (e.g. an AWS Secrets Manager
   path), never the secret value. The backend fetches the real value at
   runtime.

4. **Configuration validation**
   A Python script checks every environment's config file for missing or
   invalid settings before deployment (e.g. Prod must not use DEBUG logging).

5. **Password / secret scan**
   A Python script scans the whole repo for anything that looks like a
   password, key or token. It reports the file and line number only — never
   the value. A scan failure blocks the GitHub Actions pipeline.

6. **Drift comparison**
   A Python script compares two environments' config and reports any
   difference that is not on an "allowed to differ" list.

7. **Rotation workflow**
   A documented and scripted process to change the database secret safely:
   create new secret → overlap window (old + new both valid) → app picks up
   new secret → verify → remove old secret, or roll back if verification
   fails.

8. **Access audit**
   One IAM role per environment. Each role can read only its own secrets.
   Every permission has a one-line justification in `docs/iam.md`.

9. **Deployment verification**
   After every deployment, a script calls the app's health and info
   endpoints and confirms: app is UP, environment name matches, and the
   secret fingerprint matches what was just deployed.

10. **Build once, promote**
    GitHub Actions builds one Docker image per push and promotes the *same*
    image tag through Dev → Test → Prod. No environment ever gets its own
    rebuild.

11. **No secrets in logs**
    The backend logs only a secret's 8-character SHA-256 fingerprint, never
    the raw value. Verified as part of the final leak check.

12. **Local development mode**
    Developers run the app locally against a Dockerised Postgres with a
    fake, git-ignored password. No developer ever touches a real Dev/Test/Prod
    secret.

## Extra features (add only after all 12 above work end to end)

- Upload page (React) showing current env, build version, secret fingerprint
- Dashboard: latest validate/drift/scan results in one view
- History page: every scan/drift/rotation/deployment event, newest first
- Automatic rotation schedule (e.g. every 30 days)
- Email/Slack alert on drift or a scan failure
- Downloadable PDF/CSV evidence report
- Login + roles (only an admin can trigger rotation)
- Manual approval gate before Prod deployment

## Out of scope (do not build)

- Anything that stores or displays a raw secret value anywhere (UI, log, DB,
  file, screenshot)
- Multiple apps — this is ONE app with THREE configurations, not three apps
