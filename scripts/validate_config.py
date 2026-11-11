#!/usr/bin/env python3
"""validate_config.py - confirm an environment's config is deploy-ready.

Usage:
    python scripts/validate_config.py <dev|test|prod> [--help]

What it does:
    Loads config/<env>.env and checks that it has everything the backend
    needs to start: every required key present and non-empty (APP_VERSION
    only needs to exist — it's injected per-deploy with the promoted
    image tag, not baked into the checked-in file), APP_ENV matching the
    environment argument, DB_URL pointing at MySQL,
    DB_SECRET_REF matching the <env>/app/<name> Secrets Manager reference
    format (not a literal secret), AUDIT_DB_URL/AUDIT_DB_USERNAME/
    AUDIT_DB_SECRET_REF being present and well-formed (the shared audit
    database connection, identical across all three environments),
    WRITE_API_KEY_SECRET_REF being present and well-formed (this
    environment's own write-API key, like DB_SECRET_REF),
    and — as a second, independent check — that no line in the file
    matches the same hardcoded-secret pattern scan_secrets.py looks for
    (config files should only ever hold references).

    Prints a PASS/FAIL line per check, then an overall summary.

Exit code:
    0 - every check passed
    1 - at least one check failed
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from scan_secrets import line_has_finding  # noqa: E402

REPO_ROOT = Path(__file__).resolve().parent.parent
CONFIG_DIR = REPO_ROOT / "config"

REF_PATTERN = re.compile(r"^(dev|test|prod)/app/[a-z0-9-]+$")
AUDIT_REF_PATTERN = re.compile(r"^shared/app/[a-z0-9-]+$")

REQUIRED_KEYS = [
    "APP_ENV",
    "APP_VERSION",
    "LOG_LEVEL",
    "DB_URL",
    "DB_USERNAME",
    "DB_SECRET_REF",
    "AUDIT_DB_URL",
    "AUDIT_DB_USERNAME",
    "AUDIT_DB_SECRET_REF",
    "WRITE_API_KEY_SECRET_REF",
    "SECRETS_PROVIDER",
]


def parse_env_file(lines: list[str]) -> dict[str, str]:
    values: dict[str, str] = {}
    for line in lines:
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        if "=" not in stripped:
            continue
        key, _, value = stripped.partition("=")
        values[key.strip()] = value.strip()
    return values


def run_checks(environment: str, lines: list[str]) -> list[tuple[bool, str]]:
    values = parse_env_file(lines)
    checks: list[tuple[bool, str]] = []

    for key in REQUIRED_KEYS:
        exists = key in values
        if key == "APP_VERSION":
            # Injected per-deploy with the promoted image tag/SHA (build
            # once, promote) — the checked-in file only needs the key to
            # exist, not a value baked in ahead of time.
            checks.append((exists, f"{key} key is present (value injected at deploy time)"))
            continue
        non_empty = bool(values.get(key, "").strip())
        checks.append((exists and non_empty, f"{key} is present and non-empty"))

    app_env = values.get("APP_ENV", "").strip()
    checks.append(
        (app_env == environment, f"APP_ENV matches the '{environment}' argument")
    )

    db_url = values.get("DB_URL", "").strip()
    checks.append(
        (db_url.startswith("jdbc:mysql://"), "DB_URL starts with jdbc:mysql://")
    )

    db_secret_ref = values.get("DB_SECRET_REF", "").strip()
    checks.append(
        (
            bool(REF_PATTERN.match(db_secret_ref)),
            "DB_SECRET_REF is a Secrets Manager reference (<env>/app/<name>)",
        )
    )

    audit_db_url = values.get("AUDIT_DB_URL", "").strip()
    checks.append(
        (audit_db_url.startswith("jdbc:mysql://"), "AUDIT_DB_URL starts with jdbc:mysql://")
    )

    audit_db_secret_ref = values.get("AUDIT_DB_SECRET_REF", "").strip()
    checks.append(
        (
            bool(AUDIT_REF_PATTERN.match(audit_db_secret_ref)),
            "AUDIT_DB_SECRET_REF is a Secrets Manager reference (shared/app/<name>)",
        )
    )

    write_api_key_secret_ref = values.get("WRITE_API_KEY_SECRET_REF", "").strip()
    checks.append(
        (
            bool(REF_PATTERN.match(write_api_key_secret_ref)),
            "WRITE_API_KEY_SECRET_REF is a Secrets Manager reference (<env>/app/<name>)",
        )
    )

    secret_lines = [
        i for i, line in enumerate(lines, start=1) if line_has_finding(line)
    ]
    detail = "" if not secret_lines else f" (line {', '.join(map(str, secret_lines))})"
    checks.append(
        (
            not secret_lines,
            "No line matches the hardcoded-secret pattern (scan_secrets check)"
            + detail,
        )
    )

    return checks


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Validate that config/<env>.env has everything the backend "
            "needs before the pipeline deploys with it."
        )
    )
    parser.add_argument(
        "environment",
        choices=["dev", "test", "prod"],
        help="Which environment's config to validate (maps to config/<env>.env)",
    )
    args = parser.parse_args()

    config_path = CONFIG_DIR / f"{args.environment}.env"
    print(f"=== Validating {config_path.relative_to(REPO_ROOT)} "
          f"(environment: {args.environment}) ===")

    if not config_path.is_file():
        print(f"[FAIL] config file exists: {config_path}")
        print()
        print("Summary: 0/1 checks passed")
        print("RESULT: FAIL")
        return 1

    with open(config_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    checks = run_checks(args.environment, lines)

    for passed, message in checks:
        label = "PASS" if passed else "FAIL"
        print(f"[{label}] {message}")

    passed_count = sum(1 for passed, _ in checks if passed)
    total = len(checks)
    overall = passed_count == total

    print()
    print(f"Summary: {passed_count}/{total} checks passed")
    print(f"RESULT: {'PASS' if overall else 'FAIL'}")

    return 0 if overall else 1


if __name__ == "__main__":
    sys.exit(main())
