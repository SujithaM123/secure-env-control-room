package com.cap48.web.dto;

import java.time.Instant;

/** Never carries the new secret value - fingerprints only. */
public record RotationResultDto(String environment, String status, String oldFingerprint, String newFingerprint,
                                 Instant completedAt) {
}
