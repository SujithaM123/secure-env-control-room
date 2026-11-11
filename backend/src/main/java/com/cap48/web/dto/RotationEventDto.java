package com.cap48.web.dto;

import java.time.Instant;

public record RotationEventDto(
        Long id,
        Instant startedAt,
        Instant overlapEndsAt,
        Instant completedAt,
        String oldFingerprint,
        String newFingerprint,
        String status,
        String commitSha
) {
}
