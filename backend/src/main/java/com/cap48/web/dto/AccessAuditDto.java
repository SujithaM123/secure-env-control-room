package com.cap48.web.dto;

import java.time.Instant;

public record AccessAuditDto(
        Long id,
        String iamRole,
        String permission,
        String justification,
        Instant capturedAt
) {
}
