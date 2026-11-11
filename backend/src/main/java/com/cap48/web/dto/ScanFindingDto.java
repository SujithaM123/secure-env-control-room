package com.cap48.web.dto;

import java.time.Instant;

public record ScanFindingDto(
        Long id,
        Instant scannedAt,
        String filePath,
        int lineNumber,
        String ruleMatched,
        String commitSha
) {
}
