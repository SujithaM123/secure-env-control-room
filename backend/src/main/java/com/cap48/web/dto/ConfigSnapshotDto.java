package com.cap48.web.dto;

import java.time.Instant;
import java.util.Map;

/** settings never includes a secret value — only non-secret config and references. */
public record ConfigSnapshotDto(Instant capturedAt, Map<String, Object> settings) {
}
