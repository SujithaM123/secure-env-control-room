package com.cap48.web.dto;

import java.util.Map;

/** A point-in-time copy of one environment's non-secret settings. Never a password. */
public record ConfigSnapshotWriteRequest(String environment, Map<String, Object> settings) {
}
