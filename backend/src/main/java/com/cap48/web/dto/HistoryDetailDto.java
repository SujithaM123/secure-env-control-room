package com.cap48.web.dto;

import java.util.List;

public record HistoryDetailDto(
        String commitSha,
        List<ScanFindingDto> scanFindings,
        List<ValidationResultDto> validationResults,
        List<DriftFindingDto> driftFindings,
        List<DeploymentDto> deployments,
        List<RotationEventDto> rotationEvents
) {
}
