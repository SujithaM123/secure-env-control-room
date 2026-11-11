package com.cap48.web.dto;

/**
 * healthScore is a derived 0-100 composite of validation status, drift
 * detection, and secret freshness — not a stored value. See
 * EnvironmentsController#computeHealthScore for the formula.
 */
public record EnvironmentSummaryDto(
        String name,
        String latestValidationStatus,
        boolean driftDetected,
        Long secretAgeDays,
        boolean rotationOverdue,
        int healthScore
) {
}
