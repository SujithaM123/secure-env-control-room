package com.cap48.web.dto;

import java.time.Instant;
import java.util.List;

/**
 * One row per commit_sha, aggregated across deployments (keyed by
 * image_version), scan_findings, and the commit_sha columns added to
 * validation_results / drift_findings / rotation_events. pipelineResult,
 * scanResult, validationResult and driftResult are all derived summaries,
 * not stored columns — see HistoryController for exactly how.
 */
public record HistoryRowDto(
        String commitSha,
        Instant date,
        String pipelineResult,
        String scanResult,
        String validationResult,
        String driftResult,
        List<String> deployedTo
) {
}
