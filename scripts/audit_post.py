#!/usr/bin/env python3
"""audit_post.py - shared helper for CI scripts to record results into the
shared cap48_audit database via the backend's write endpoints.

Posting is always best-effort: a failed post prints a warning and never
changes a calling script's own exit code - these are audit-trail writes,
not the thing the pipeline gates on. The API key is read from an
environment variable, never a command-line argument, so it never shows up
in a process list or a logged command line.
"""

from __future__ import annotations

import json
import os
from urllib import error as urlerror
from urllib import request

REQUEST_TIMEOUT_SECONDS = 5


def api_key_from_env(var_name: str = "WRITE_API_KEY") -> str | None:
    return os.environ.get(var_name) or None


def post(base_url: str, path: str, api_key: str, body: dict) -> bool:
    """POSTs body as JSON to {base_url}{path} with the X-Write-Api-Key
    header. Returns True on a 2xx response, False otherwise - never raises."""
    url = base_url.rstrip("/") + path
    data = json.dumps(body).encode("utf-8")
    req = request.Request(
        url,
        data=data,
        method="POST",
        headers={
            "Content-Type": "application/json",
            "X-Write-Api-Key": api_key,
        },
    )
    try:
        with request.urlopen(req, timeout=REQUEST_TIMEOUT_SECONDS) as resp:
            status = resp.getcode()
    except urlerror.HTTPError as e:
        print(f"  [audit] POST {path} failed: HTTP {e.code}")
        return False
    except (urlerror.URLError, OSError) as e:
        print(f"  [audit] POST {path} failed: {e}")
        return False

    if not (200 <= status < 300):
        print(f"  [audit] POST {path} returned unexpected status {status}")
        return False
    return True
