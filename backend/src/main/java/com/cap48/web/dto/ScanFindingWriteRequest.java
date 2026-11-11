package com.cap48.web.dto;

/** One hardcoded-secret hit from scan_secrets.py. Never carries the matched value. */
public record ScanFindingWriteRequest(String filePath, int lineNumber, String ruleMatched, String commitSha) {
}
