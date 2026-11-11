package com.cap48.web;

import com.cap48.db.*;
import com.cap48.web.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Read-only views over environments and everything scoped to one: config
 * snapshots, rotation history, deployment history, and access audit. Never
 * returns a secret value — config_snapshots only ever stores non-secret
 * settings, and rotation fingerprints are 8-char hashes, not secrets.
 */
@RestController
public class EnvironmentsController {

    private static final List<String> ENV_ORDER = List.of("dev", "test", "prod");

    /** Below this many days since the last completed rotation, a secret counts as fresh. */
    private static final long ROTATION_FRESH_DAYS = 30;
    /** At or beyond this many days, a secret counts as fully overdue (score floor, flagged overdue). */
    private static final long ROTATION_OVERDUE_DAYS = 90;

    private final EnvironmentRepository environments;
    private final ConfigSnapshotRepository configSnapshots;
    private final ValidationResultRepository validationResults;
    private final DriftFindingRepository driftFindings;
    private final RotationEventRepository rotationEvents;
    private final DeploymentRepository deployments;
    private final AccessAuditRepository accessAudits;
    private final ObjectMapper objectMapper;

    public EnvironmentsController(
            EnvironmentRepository environments,
            ConfigSnapshotRepository configSnapshots,
            ValidationResultRepository validationResults,
            DriftFindingRepository driftFindings,
            RotationEventRepository rotationEvents,
            DeploymentRepository deployments,
            AccessAuditRepository accessAudits,
            ObjectMapper objectMapper
    ) {
        this.environments = environments;
        this.configSnapshots = configSnapshots;
        this.validationResults = validationResults;
        this.driftFindings = driftFindings;
        this.rotationEvents = rotationEvents;
        this.deployments = deployments;
        this.accessAudits = accessAudits;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/api/environments")
    public List<String> listEnvironments() {
        return ENV_ORDER;
    }

    @GetMapping("/api/environments/summary")
    public List<EnvironmentSummaryDto> allSummaries() {
        return ENV_ORDER.stream().map(this::summaryFor).toList();
    }

    @GetMapping("/api/environments/{name}/summary")
    public EnvironmentSummaryDto summary(@PathVariable String name) {
        requireEnvironment(name);
        return summaryFor(name);
    }

    @GetMapping("/api/environments/{name}/config-snapshot")
    public ConfigSnapshotDto configSnapshot(@PathVariable String name) {
        Environment env = requireEnvironment(name);
        ConfigSnapshot snapshot = configSnapshots.findTopByEnvironmentIdOrderByCapturedAtDesc(env.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No config snapshot recorded for " + name));
        return new ConfigSnapshotDto(snapshot.getCapturedAt(), readMap(snapshot.getSettingsJson()));
    }

    @GetMapping("/api/environments/{name}/rotations")
    public List<RotationEventDto> rotations(@PathVariable String name) {
        Environment env = requireEnvironment(name);
        return rotationEvents.findByEnvironmentIdOrderByStartedAtDesc(env.getId()).stream()
                .map(r -> new RotationEventDto(
                        r.getId(), r.getStartedAt(), r.getOverlapEndsAt(), r.getCompletedAt(),
                        r.getOldFingerprint(), r.getNewFingerprint(), r.getStatus(), r.getCommitSha()))
                .toList();
    }

    @GetMapping("/api/environments/{name}/deployments")
    public List<DeploymentDto> deployments(@PathVariable String name) {
        Environment env = requireEnvironment(name);
        return deployments.findByEnvironmentIdOrderByDeployedAtDesc(env.getId()).stream()
                .map(d -> toDeploymentDto(d, name))
                .toList();
    }

    @GetMapping("/api/environments/{name}/access-audit")
    public List<AccessAuditDto> accessAudit(@PathVariable String name) {
        Environment env = requireEnvironment(name);
        return accessAudits.findByEnvironmentIdOrderByCapturedAtDescIdDesc(env.getId()).stream()
                .map(a -> new AccessAuditDto(a.getId(), a.getIamRole(), a.getPermission(),
                        a.getJustification(), a.getCapturedAt()))
                .toList();
    }

    private EnvironmentSummaryDto summaryFor(String name) {
        Environment env = requireEnvironment(name);

        String latestValidationStatus = validationResults
                .findTopByEnvironmentIdOrderByCheckedAtDesc(env.getId())
                .map(ValidationResult::getStatus)
                .orElse("UNKNOWN");

        boolean driftDetected = driftFindings.findLatestComparedAt()
                .map(driftFindings::findByAllowedFalseAndComparedAt)
                .map(rows -> rows.stream().anyMatch(d ->
                        d.getEnvA().getName().equals(name) || d.getEnvB().getName().equals(name)))
                .orElse(false);

        Long secretAgeDays = rotationEvents
                .findTopByEnvironmentIdAndStatusOrderByCompletedAtDesc(env.getId(), "switched")
                .map(RotationEvent::getCompletedAt)
                .map(completedAt -> Duration.between(completedAt, Instant.now()).toDays())
                .orElse(null);

        boolean rotationOverdue = secretAgeDays == null || secretAgeDays >= ROTATION_OVERDUE_DAYS;

        int healthScore = computeHealthScore(latestValidationStatus, driftDetected, secretAgeDays);

        return new EnvironmentSummaryDto(name, latestValidationStatus, driftDetected, secretAgeDays,
                rotationOverdue, healthScore);
    }

    /**
     * Simple 0-100 freshness/health score, averaging three components:
     * validation (100 PASS / 0 FAIL / 50 UNKNOWN), drift (100 none / 0 any),
     * and rotation freshness (100 at or under {@link #ROTATION_FRESH_DAYS},
     * linearly down to 0 at or over {@link #ROTATION_OVERDUE_DAYS}, 0 if
     * never rotated). This is a presentation heuristic, not a stored metric.
     */
    static int computeHealthScore(String latestValidationStatus, boolean driftDetected, Long secretAgeDays) {
        int validationComponent = switch (latestValidationStatus) {
            case "PASS" -> 100;
            case "FAIL" -> 0;
            default -> 50;
        };
        int driftComponent = driftDetected ? 0 : 100;
        int rotationComponent;
        if (secretAgeDays == null) {
            rotationComponent = 0;
        } else if (secretAgeDays <= ROTATION_FRESH_DAYS) {
            rotationComponent = 100;
        } else if (secretAgeDays >= ROTATION_OVERDUE_DAYS) {
            rotationComponent = 0;
        } else {
            double span = ROTATION_OVERDUE_DAYS - ROTATION_FRESH_DAYS;
            rotationComponent = (int) Math.round(100 * (ROTATION_OVERDUE_DAYS - secretAgeDays) / span);
        }
        return Math.round((validationComponent + driftComponent + rotationComponent) / 3f);
    }

    DeploymentDto toDeploymentDto(Deployment d, String environmentName) {
        return new DeploymentDto(d.getId(), environmentName, d.getImageVersion(), d.getDeployedAt(),
                d.isVerified(), readTree(d.getVerificationDetailsJson()));
    }

    private Environment requireEnvironment(String name) {
        return environments.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown environment: " + name));
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    JsonNode readTree(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
