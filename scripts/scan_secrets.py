#!/usr/bin/env python3
"""scan_secrets.py - fail the pipeline if a hardcoded secret is committed.

Usage:
    python scripts/scan_secrets.py [--help]

What it does:
    Scans every file tracked by git (skipping .git/, node_modules/,
    target/, and anything that looks binary) for lines that look like a
    hardcoded password, API key, token, or AWS access key. Only the file
    path and line number of each finding are ever printed or stored -
    the matched value itself is never shown. A line that is clearly a
    *reference* to a secret (an AWS Secrets Manager path, a file path, or
    ${ENV_VAR} interpolation) is not a finding and is skipped.

Exit code:
    0 - no findings (safe to continue the pipeline)
    1 - one or more findings (pipeline should stop)
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
SELF_PATH = Path(__file__).resolve()

SKIP_DIR_NAMES = {".git", "node_modules", "target", "dist", "build", ".venv"}

# Variable-name segments (split on any non-alphanumeric character) that mark
# an assignment as secret-shaped when the value is a plain quoted string.
# Bare "key" is deliberately excluded here - it's far too common a field
# name (object keys, map keys, React `key` props) to flag on its own; it
# only counts when paired with a long base64/hex-looking value below.
QUOTED_VALUE_SEGMENTS = {
    "password",
    "passwd",
    "pwd",
    "secret",
    "token",
    "apikey",
    "accesskey",
    "secretkey",
}

ASSIGNMENT_RE = re.compile(r"^(.*?)([A-Za-z_][A-Za-z0-9_]{1,60})\s*[:=]\s*(.+)$")
AWS_ACCESS_KEY_RE = re.compile(r"\bAKIA[0-9A-Z]{16}\b")
BASE64_HEX_RE = re.compile(r"^[A-Za-z0-9+/_=-]{20,}$")


class Finding:
    __slots__ = ("path", "line_number")

    def __init__(self, path: str, line_number: int):
        self.path = path
        self.line_number = line_number


def list_tracked_files() -> list[Path]:
    try:
        result = subprocess.run(
            ["git", "-C", str(REPO_ROOT), "ls-files"],
            capture_output=True,
            text=True,
            check=True,
        )
        paths = [REPO_ROOT / line for line in result.stdout.splitlines() if line]
        if paths:
            return paths
    except (subprocess.CalledProcessError, FileNotFoundError):
        pass

    # Fallback: not a git repo (or git unavailable) - walk the filesystem.
    paths = []
    for path in REPO_ROOT.rglob("*"):
        if not path.is_file():
            continue
        if any(part in SKIP_DIR_NAMES for part in path.relative_to(REPO_ROOT).parts):
            continue
        paths.append(path)
    return paths


def is_binary(path: Path) -> bool:
    try:
        with open(path, "rb") as f:
            chunk = f.read(8192)
    except OSError:
        return True
    return b"\x00" in chunk


def strip_value(raw: str) -> str:
    value = raw.strip()
    # Drop a trailing line comment or statement terminator.
    for terminator in ("//", "#"):
        idx = value.find(terminator)
        if idx > 0:
            value = value[:idx].strip()
    value = value.rstrip(";,")
    if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
        value = value[1:-1]
    return value.strip()


def is_reference_value(value: str) -> bool:
    if not value:
        return True
    if "${" in value:
        return True
    if value.startswith("/"):
        return True
    if value.startswith("arn:"):
        return True
    if value.startswith("<") and value.endswith(">"):
        return True
    return False


COMPOUND_SEGMENT_PAIRS = ("apikey", "accesskey", "secretkey")


def identifier_segments(identifier: str) -> list[str]:
    spaced = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", identifier)
    return [seg.lower() for seg in re.split(r"[^A-Za-z0-9]+", spaced) if seg]


def line_has_finding(line: str) -> bool:
    if AWS_ACCESS_KEY_RE.search(line):
        return True

    match = ASSIGNMENT_RE.match(line.strip())
    if not match:
        return False

    _, identifier, raw_value = match.groups()
    segments = identifier_segments(identifier)
    segment_set = set(segments)
    concatenated = "".join(segments)

    # password / secret / token / pwd / passwd, or a compound like api_key,
    # access_key, secret_key - these are secret-shaped regardless of how
    # the value is quoted.
    strong_hit = bool(segment_set & QUOTED_VALUE_SEGMENTS) or any(
        pair in concatenated for pair in COMPOUND_SEGMENT_PAIRS
    )
    # A bare "key" is too common a field name (object/map keys, React `key`
    # props) to flag on its own unless the value itself looks like an
    # actual secret blob.
    bare_key_hit = "key" in segment_set and not strong_hit

    if not strong_hit and not bare_key_hit:
        return False

    was_quoted = len(raw_value.strip()) >= 2 and raw_value.strip()[0] in "\"'"
    value = strip_value(raw_value)
    if is_reference_value(value):
        return False

    if strong_hit:
        if was_quoted:
            return len(value) >= 4
        # Unquoted: a bare identifier (e.g. `this.secretProvider =
        # secretProvider;`) is a variable reference, not a literal secret.
        # Real literal values here (passwords, tokens, .env entries)
        # contain at least one digit.
        return len(value) >= 4 and bool(re.search(r"\d", value))

    return bool(BASE64_HEX_RE.match(value)) and not value.isdigit()


def scan_file(path: Path) -> list[Finding]:
    findings: list[Finding] = []
    try:
        with open(path, "r", encoding="utf-8", errors="strict") as f:
            lines = f.readlines()
    except (OSError, UnicodeDecodeError):
        return findings

    rel_path = str(path.relative_to(REPO_ROOT)).replace("\\", "/")
    for line_number, line in enumerate(lines, start=1):
        if line_has_finding(line):
            findings.append(Finding(rel_path, line_number))
    return findings


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Scan the repository for hardcoded secrets. Reports file:line "
            "only - never the matched value. Exits 1 if anything is found, "
            "so GitHub Actions can fail the build on a non-zero exit."
        )
    )
    parser.parse_args()

    files = list_tracked_files()

    scanned = 0
    all_findings: list[Finding] = []
    for path in files:
        if path.resolve() == SELF_PATH:
            continue
        if any(part in SKIP_DIR_NAMES for part in path.relative_to(REPO_ROOT).parts):
            continue
        if not path.is_file() or is_binary(path):
            continue
        scanned += 1
        all_findings.extend(scan_file(path))

    print(f"Files scanned: {scanned}")
    print(f"Findings: {len(all_findings)}")
    if all_findings:
        print()
        for finding in all_findings:
            print(f"{finding.path}:{finding.line_number}")

    return 1 if all_findings else 0


if __name__ == "__main__":
    sys.exit(main())
