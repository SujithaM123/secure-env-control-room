#!/usr/bin/env python3
"""verify.py - the final gate: confirm a deployment is actually healthy.

Usage:
    python scripts/verify.py <base_url> <dev|test|prod> [--help]

Example:
    python scripts/verify.py http://dev.cap48.example.com dev

What it does:
    Calls GET {base_url}/actuator/health, retrying up to 5 times (3s
    apart) since a just-deployed app may take a moment to become
    ready, and requires HTTP 200 with a body status of "UP".

    Calls GET {base_url}/api/config-info and confirms its "env" field
    matches the expected environment exactly — a mismatch (e.g.
    expected dev, got test) is a hard failure: the wrong environment
    was deployed. The secret fingerprint it returns is printed as-is
    (it's already safe, 8 hex characters) so it can be compared
    against the fingerprint recorded for the matching rotation event.
    Nothing else from the response is ever printed.

Exit code:
    0 - health check passed AND environment matched
    1 - either check failed
"""

from __future__ import annotations

import argparse
import json
import sys
import time
from urllib import error as urlerror
from urllib import request

HEALTH_ATTEMPTS = 5
HEALTH_RETRY_DELAY_SECONDS = 3
REQUEST_TIMEOUT_SECONDS = 5


def fetch_json(url: str) -> tuple[int | None, dict | None, str | None]:
    """Returns (status_code, parsed_json_body, error_message)."""
    try:
        with request.urlopen(url, timeout=REQUEST_TIMEOUT_SECONDS) as resp:
            status = resp.getcode()
            raw = resp.read()
    except urlerror.HTTPError as e:
        status = e.code
        try:
            raw = e.read()
        except Exception:
            raw = b""
    except (urlerror.URLError, OSError) as e:
        return None, None, str(e.reason) if hasattr(e, "reason") else str(e)
    except Exception as e:  # defensive: never crash, always report a reason
        return None, None, str(e)

    try:
        body = json.loads(raw.decode("utf-8")) if raw else None
    except (json.JSONDecodeError, UnicodeDecodeError):
        body = None

    return status, body, None


def check_health(base_url: str) -> tuple[bool, str]:
    reason = "no attempts made"
    for attempt in range(1, HEALTH_ATTEMPTS + 1):
        status, body, err = fetch_json(f"{base_url}/actuator/health")

        if err:
            reason = f"connection error - {err}"
        elif status != 200:
            reason = f"HTTP {status}"
        elif not isinstance(body, dict) or body.get("status") != "UP":
            reason = f"unexpected response body: {body!r}"
        else:
            return True, "HTTP 200, status=UP"

        is_last_attempt = attempt == HEALTH_ATTEMPTS
        if is_last_attempt:
            return False, f"{reason} (after {HEALTH_ATTEMPTS} attempts)"

        print(
            f"  attempt {attempt}/{HEALTH_ATTEMPTS}: {reason} - "
            f"retrying in {HEALTH_RETRY_DELAY_SECONDS}s"
        )
        time.sleep(HEALTH_RETRY_DELAY_SECONDS)

    return False, reason  # unreachable, kept for clarity


def check_environment(
    base_url: str, expected_env: str
) -> tuple[bool, str, str | None]:
    status, body, err = fetch_json(f"{base_url}/api/config-info")

    if err:
        return False, f"connection error - {err}", None
    if status != 200:
        return False, f"HTTP {status}", None
    if not isinstance(body, dict):
        return False, "response was not valid JSON", None

    fingerprint = body.get("secretFingerprint")
    actual_env = body.get("env")

    if actual_env != expected_env:
        return (
            False,
            f"expected env='{expected_env}' but got env='{actual_env}'",
            fingerprint,
        )
    return True, f"env='{expected_env}' as expected", fingerprint


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Post-deployment verification: confirm the app at base_url is "
            "UP and reports the expected environment, before the deploy "
            "counts as successful."
        )
    )
    parser.add_argument("base_url", help="e.g. http://dev.cap48.example.com")
    parser.add_argument(
        "environment",
        choices=["dev", "test", "prod"],
        help="The environment that should be deployed at base_url",
    )
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/")

    print(f"=== Verifying {base_url} (expected environment: {args.environment}) ===")

    health_ok, health_reason = check_health(base_url)
    print(f"[{'PASS' if health_ok else 'FAIL'}] Health check: {health_reason}")

    env_ok, env_reason, fingerprint = check_environment(base_url, args.environment)
    print(f"[{'PASS' if env_ok else 'FAIL'}] Environment check: {env_reason}")

    print(f"Secret fingerprint: {fingerprint if fingerprint else 'n/a'}")

    passed = int(health_ok) + int(env_ok)
    print()
    print(f"Summary: {passed}/2 checks passed")
    overall = health_ok and env_ok
    print(f"RESULT: {'PASS' if overall else 'FAIL'}")

    return 0 if overall else 1


if __name__ == "__main__":
    sys.exit(main())
