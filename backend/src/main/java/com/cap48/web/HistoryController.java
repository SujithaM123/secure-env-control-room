package com.cap48.web;

import com.cap48.db.*;
import com.cap48.web.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * Commit-level aggregation across deployments (keyed by image_version),
 * scan_findings, and the commit_sha columns added to validation_results,
 * drift_findings and rotation_events. pipelineResult/scanResult/
 * validationResult/driftResult are derived summaries computed here, not
 * stored columns.
 */
@RestController
public class HistoryController {

    private final DeploymentRepository deployments;
    private final ScanFindingRepository scanFindings;
    private final ValidationResultRepository validationResults;
    private final DriftFindingRepository driftFindings;
    private final RotationEventRepository rotationEvents;
    private final ObjectMapper objectMapper;

    public HistoryController(
            DeploymentRepository deployments,
            ScanFindingRepository scanFindings,
            ValidationResultRepository validationResults,
            DriftFindingRepository driftFindings,
            RotationEventRepository rotationEvents,
            ObjectMapper objectMapper
    ) {
        this.deployments = deployments;
        this.scanFindings = scanFindings;
        this.validationResults = validationResults;
        this.driftFindings = driftFindings;
        this.rotationEvents = rotationEvents;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/api/history")
    public List<HistoryRowDto> history() {
        Set<String> commitShas = new HashSet<>();
        commitShas.addAll(deployments.findDistinctImageVersions());
        commitShas.addAll(scanFindings.findDistinctCommitShas());
        commitShas.addAll(validationResults.findDistinctCommitShas());
        commitShas.addAll(driftFindings.findDistinctCommitShas());
        commitShas.addAll(rotationEvents.findDistinctCommitShas());
        commitShas.removeIf(sha -> sha == null || sha.isBlank());

        return commitShas.stream()
                .map(this::rowFor)
                .sorted(Comparator.comparing(HistoryRowDto::date,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @GetMapping("/api/history/{commitSha}")
    public HistoryDetailDto detail(@PathVariable String commitSha) {
        List<ScanFinding> scans = scanFindings.findByCommitSha(commitSha);
        List<ValidationResult> validations = validationResults.findByCommitSha(commitSha);
        List<DriftFinding> drifts = driftFindings.findByCommitSha(commitSha);
        List<Deployment> deploys = deployments.findByImageVersion(commitSha);
        List<RotationEvent> rotations = rotationEvents.findByCommitSha(commitSha);

        if (scans.isEmpty() && validations.isEmpty() && drifts.isEmpty()
                && deploys.isEmpty() && rotations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No history for commit " + commitSha);
        }

        return new HistoryDetailDto(
                commitSha,
                scans.stream().map(s -> new ScanFindingDto(s.getId(), s.getScannedAt(), s.getFilePath(),
                        s.getLineNumber(), s.getRuleMatched(), s.getCommitSha())).toList(),
                validations.stream().map(v -> new ValidationResultDto(v.getEnvironment().getName(),
                        v.getStatus(), v.getCheckedAt(), readTree(v.getErrorsJson()))).toList(),
                drifts.stream().map(d -> new DriftFindingDto(d.getEnvA().getName(), d.getEnvB().getName(),
                        d.getSettingKey(), d.getValueA(), d.getValueB(), d.isAllowed())).toList(),
                deploys.stream().map(d -> new DeploymentDto(d.getId(), d.getEnvironment().getName(),
                        d.getImageVersion(), d.getDeployedAt(), d.isVerified(),
                        readTree(d.getVerificationDetailsJson()))).toList(),
                rotations.stream().map(r -> new RotationEventDto(r.getId(), r.getStartedAt(),
                        r.getOverlapEndsAt(), r.getCompletedAt(), r.getOldFingerprint(),
                        r.getNewFingerprint(), r.getStatus(), r.getCommitSha())).toList()
        );
    }

    private HistoryRowDto rowFor(String commitSha) {
        List<Deployment> deploys = deployments.findByImageVersion(commitSha);
        List<ScanFinding> scans = scanFindings.findByCommitSha(commitSha);
        List<ValidationResult> validations = validationResults.findByCommitSha(commitSha);
        List<DriftFinding> drifts = driftFindings.findByCommitSha(commitSha);
        List<RotationEvent> rotations = rotationEvents.findByCommitSha(commitSha);

        List<String> deployedTo = deploys.stream().map(d -> d.getEnvironment().getName()).toList();

        String scanResult = scans.isEmpty() ? "PASS" : "FAIL";
        String validationResult = validations.isEmpty() ? "UNKNOWN"
                : validations.stream().anyMatch(v -> "FAIL".equals(v.getStatus())) ? "FAIL" : "PASS";
        String driftResult = drifts.isEmpty() ? "UNKNOWN"
                : drifts.stream().anyMatch(d -> !d.isAllowed()) ? "FAIL" : "PASS";

        String pipelineResult;
        if ("FAIL".equals(scanResult) || "FAIL".equals(validationResult)) {
            pipelineResult = "FAILED";
        } else if (!deployedTo.isEmpty()) {
            pipelineResult = "SUCCESS";
        } else {
            pipelineResult = "UNKNOWN";
        }

        Instant date = Stream.of(
                        deploys.stream().map(Deployment::getDeployedAt),
                        scans.stream().map(ScanFinding::getScannedAt),
                        validations.stream().map(ValidationResult::getCheckedAt),
                        drifts.stream().map(DriftFinding::getComparedAt),
                        rotations.stream().map(RotationEvent::getStartedAt)
                )
                .flatMap(s -> s)
                .min(Comparator.naturalOrder())
                .orElse(null);

        return new HistoryRowDto(commitSha, date, pipelineResult, scanResult, validationResult,
                driftResult, deployedTo);
    }

    private JsonNode readTree(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
