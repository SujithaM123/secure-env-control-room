#!/usr/bin/env python3
"""drift.py - flag unexpected config differences between environments.

Usage:
    python scripts/drift.py [--post-url URL] [--commit-sha SHA] [--help]

What it does:
    Reads config/dev.env, config/test.env, and config/prod.env, and
    compares every pair of environments (dev<->test, test<->prod,
    dev<->prod). Keys on the allowed-to-differ list (DB_URL,
    DB_USERNAME, LOG_LEVEL, APP_ENV, APP_VERSION, DB_SECRET_REF,
    WRITE_API_KEY_SECRET_REF) are expected to vary and are skipped.
    Anything else that differs —
    including a key that exists in one environment's file but is
    entirely missing from another's, which is flagged as the more
    serious case — is unexpected drift.

    Values are printed in the report so you can see what actually
    differs, but each one is first checked against the same
    hardcoded-secret pattern scan_secrets.py uses; a match is redacted
    rather than printed (config files should only ever hold references,
    so this should never trigger, but the report double-checks anyway).

    If --post-url is given and the WRITE_API_KEY environment variable is
    set, each pair's unexpected drift items (using the same redacted
    values as the report) are also recorded to
    {post-url}/api/drift-findings for the audit trail (best-effort - a
    post failure never changes this script's own exit code). A pair
    with no unexpected drift posts nothing.

Exit code:
    0 - no unexpected drift
    1 - at least one unexpected drift item found
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import audit_post  # noqa: E402
from scan_secrets import line_has_finding  # noqa: E402
from validate_config import parse_env_file  # noqa: E402

REPO_ROOT = Path(__file__).resolve().parent.parent
CONFIG_DIR = REPO_ROOT / "config"

ENVIRONMENTS = ["dev", "test", "prod"]
PAIRS = [("dev", "test"), ("test", "prod"), ("dev", "prod")]

ALLOWED_TO_DIFFER = {
    "DB_URL",
    "DB_USERNAME",
    "LOG_LEVEL",
    "APP_ENV",
    "APP_VERSION",
    # Each environment points at its own Secrets Manager path, same as it
    # has its own DB_URL/DB_USERNAME — expected to differ, not drift.
    "DB_SECRET_REF",
    # Same reasoning — each environment has its own write-API key.
    "WRITE_API_KEY_SECRET_REF",
}

MISSING = object()


def load_env(name: str) -> dict[str, str]:
    path = CONFIG_DIR / f"{name}.env"
    with open(path, "r", encoding="utf-8") as f:
        return parse_env_file(f.readlines())


def safe_display(key: str, value: str) -> str:
    if line_has_finding(f"{key}={value}"):
        return "[REDACTED - value looks secret-shaped]"
    return value


def safe_display_or_none(key: str, value: object) -> str | None:
    if value is MISSING:
        return None
    return safe_display(key, value)


def compare(
    env_a: str, values_a: dict[str, str], env_b: str, values_b: dict[str, str]
) -> list[tuple[str, str, object, object]]:
    keys = sorted(set(values_a) | set(values_b))
    findings = []
    for key in keys:
        if key in ALLOWED_TO_DIFFER:
            continue
        a_val = values_a[key] if key in values_a else MISSING
        b_val = values_b[key] if key in values_b else MISSING
        if a_val is MISSING or b_val is MISSING:
            findings.append(("MISSING", key, a_val, b_val))
        elif a_val != b_val:
            findings.append(("DIFFERS", key, a_val, b_val))
    return findings


def print_pair_report(env_a: str, env_b: str, findings: list[tuple[str, str, object, object]]) -> None:
    print(f"=== {env_a} vs {env_b} ===")
    if not findings:
        print("No unexpected drift.")
        print()
        return

    for kind, key, a_val, b_val in findings:
        a_display = "<missing>" if a_val is MISSING else safe_display(key, a_val)
        b_display = "<missing>" if b_val is MISSING else safe_display(key, b_val)
        tag = "[MISSING KEY]" if kind == "MISSING" else "[VALUE DIFFERS]"
        print(f"{tag} {key}: {env_a}={a_display} | {env_b}={b_display}")
    print()


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Compare config/dev.env, test.env, and prod.env and flag any "
            "key that differs or is missing across environments, other "
            "than the keys expected to vary."
        )
    )
    parser.add_argument("--post-url", help="If given, POST each pair's drift items to {post-url}/api/drift-findings")
    parser.add_argument(
        "--commit-sha",
        default=os.environ.get("GITHUB_SHA", ""),
        help="Commit SHA recorded with each pair's drift items (defaults to $GITHUB_SHA)",
    )
    args = parser.parse_args()

    values = {name: load_env(name) for name in ENVIRONMENTS}

    api_key = None
    if args.post_url:
        api_key = audit_post.api_key_from_env()
        if not api_key:
            print("[audit] --post-url given but WRITE_API_KEY is not set - skipping audit recording")

    total = 0
    for env_a, env_b in PAIRS:
        findings = compare(env_a, values[env_a], env_b, values[env_b])
        total += len(findings)
        print_pair_report(env_a, env_b, findings)

        if api_key and findings:
            items = [
                {
                    "settingKey": key,
                    "valueA": safe_display_or_none(key, a_val),
                    "valueB": safe_display_or_none(key, b_val),
                    "allowed": False,
                }
                for _kind, key, a_val, b_val in findings
            ]
            audit_post.post(args.post_url, "/api/drift-findings", api_key, {
                "envA": env_a,
                "envB": env_b,
                "commitSha": args.commit_sha,
                "findings": items,
            })

    print(f"Summary: {total} unexpected drift item(s) found across all pairs")
    print("RESULT: " + ("FAIL" if total else "PASS"))

    return 1 if total else 0


if __name__ == "__main__":
    sys.exit(main())
