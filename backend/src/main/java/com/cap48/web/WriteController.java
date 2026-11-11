package com.cap48.web;

import com.cap48.db.*;
import com.cap48.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * The only way the 8 audit tables get populated — CI pipeline scripts call
 * these, authenticated via {@link WriteApiKeyFilter}. Never accepts a
 * secret value: scan findings never carry the matched string, and drift
 * values must already be redacted by the caller (drift.py's own
 * safe_display) before they reach here.
 */
@RestController
public class WriteController {

    private final EnvironmentResolver environmentResolver;
    private final ScanFindingRepository scanFindings;
    private final ValidationResultRepository validationResults;
    private final DriftFindingRepository driftFindings;
    private final ConfigSnapshotRepository configSnapshots;
    private final DeploymentRepository deployments;
    private final ObjectMapper objectMapper;

    public WriteController(
            EnvironmentResolver environmentResolver,
            ScanFindingRepository scanFindings,
            ValidationResultRepository validationResults,
            DriftFindingRepository driftFindings,
            ConfigSnapshotRepository configSnapshots,
            DeploymentRepository deployments,
            ObjectMapper objectMapper
    ) {
        this.environmentResolver = environmentResolver;
        this.scanFindings = scanFindings;
        this.validationResults = validationResults;
        this.driftFindings = driftFindings;
        this.configSnapshots = configSnapshots;
        this.deployments = deployments;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/scan-findings")
    public ResponseEntity<WriteResultDto> createScanFinding(@RequestBody ScanFindingWriteRequest request) {
        ScanFinding finding = new ScanFinding();
        finding.setScannedAt(Instant.now());
        finding.setFilePath(request.filePath());
        finding.setLineNumber(request.lineNumber());
        finding.setRuleMatched(request.ruleMatched());
        finding.setCommitSha(request.commitSha());
        scanFindings.save(finding);
        return created(finding.getId());
    }

    @PostMapping("/api/validation-results")
    public ResponseEntity<WriteResultDto> createValidationResult(@RequestBody ValidationResultWriteRequest request) {
        Environment env = requireEnvironment(request.environment());
        ValidationResult result = new ValidationResult();
        result.setEnvironment(env);
        result.setCheckedAt(Instant.now());
        result.setStatus(request.status());
        result.setErrorsJson(writeJson(request.errors()));
        result.setCommitSha(request.commitSha());
        validationResults.save(result);
        return created(result.getId());
    }

    @PostMapping("/api/drift-findings")
    public ResponseEntity<WriteResultDto> createDriftFindings(@RequestBody DriftFindingsWriteRequest request) {
        Environment envA = requireEnvironment(request.envA());
        Environment envB = requireEnvironment(request.envB());
        Instant comparedAt = Instant.now();
        Long lastId = null;
        for (DriftFindingsWriteRequest.Item item : request.findings()) {
            DriftFinding finding = new DriftFinding();
            finding.setComparedAt(comparedAt);
            finding.setEnvA(envA);
            finding.setEnvB(envB);
            finding.setSettingKey(item.settingKey());
            finding.setValueA(item.valueA());
            finding.setValueB(item.valueB());
            finding.setAllowed(item.allowed());
            finding.setCommitSha(request.commitSha());
            driftFindings.save(finding);
            lastId = finding.getId();
        }
        return created(lastId);
    }

    @PostMapping("/api/config-snapshots")
    public ResponseEntity<WriteResultDto> createConfigSnapshot(@RequestBody ConfigSnapshotWriteRequest request) {
        Environment env = requireEnvironment(request.environment());
        ConfigSnapshot snapshot = new ConfigSnapshot();
        snapshot.setEnvironment(env);
        snapshot.setCapturedAt(Instant.now());
        snapshot.setSettingsJson(writeJson(request.settings()));
        configSnapshots.save(snapshot);
        return created(snapshot.getId());
    }

    @PostMapping("/api/deployments")
    public ResponseEntity<WriteResultDto> createDeployment(@RequestBody DeploymentWriteRequest request) {
        Environment env = requireEnvironment(request.environment());
        Deployment deployment = new Deployment();
        deployment.setEnvironment(env);
        deployment.setImageVersion(request.imageVersion());
        deployment.setDeployedAt(Instant.now());
        deployment.setVerified(request.verified());
        deployment.setVerificationDetailsJson(writeJson(request.verificationDetails()));
        deployments.save(deployment);
        return created(deployment.getId());
    }

    private Environment requireEnvironment(String name) {
        try {
            return environmentResolver.resolve(name);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private String writeJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not serialize request body to JSON");
        }
    }

    private ResponseEntity<WriteResultDto> created(Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new WriteResultDto(id));
    }
}
