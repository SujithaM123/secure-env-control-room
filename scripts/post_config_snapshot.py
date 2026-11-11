#!/usr/bin/env python3
"""post_config_snapshot.py - record a point-in-time copy of an environment's
non-secret settings into the shared audit database.

Usage:
    WRITE_API_KEY=... python scripts/post_config_snapshot.py <dev|test|prod> <base_url>

What it does:
    Reads config/<env>.env, parses it the same way validate_config.py
    does, and POSTs the resulting key/value pairs to
    {base_url}/api/config-snapshots. config/*.env never holds an actual
    secret value - DB_SECRET_REF and friends are references, not
    passwords, which scan_secrets.py enforces - so the whole parsed file
    is safe to store as-is.

Exit code:
    0 - posted successfully, or WRITE_API_KEY is unset (treated as opt-out)
    1 - WRITE_API_KEY is set but the POST failed
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import audit_post  # noqa: E402
from validate_config import parse_env_file  # noqa: E402

REPO_ROOT = Path(__file__).resolve().parent.parent
CONFIG_DIR = REPO_ROOT / "config"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("environment", choices=["dev", "test", "prod"])
    parser.add_argument("base_url", help="The environment's own base URL - it records its own settings")
    args = parser.parse_args()

    api_key = audit_post.api_key_from_env()
    if not api_key:
        print("[audit] WRITE_API_KEY is not set - skipping config snapshot")
        return 0

    config_path = CONFIG_DIR / f"{args.environment}.env"
    with open(config_path, "r", encoding="utf-8") as f:
        settings = parse_env_file(f.readlines())

    ok = audit_post.post(args.base_url, "/api/config-snapshots", api_key, {
        "environment": args.environment,
        "settings": settings,
    })
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
