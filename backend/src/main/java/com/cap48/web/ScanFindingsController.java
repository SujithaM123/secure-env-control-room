package com.cap48.web;

import com.cap48.db.ScanFindingRepository;
import com.cap48.web.dto.ScanFindingDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Standalone secret-scan findings, not tied to an environment — keyed by commit_sha. */
@RestController
public class ScanFindingsController {

    private final ScanFindingRepository scanFindings;

    public ScanFindingsController(ScanFindingRepository scanFindings) {
        this.scanFindings = scanFindings;
    }

    @GetMapping("/api/scan-findings")
    public List<ScanFindingDto> all() {
        return scanFindings.findAllByOrderByScannedAtDesc().stream()
                .map(s -> new ScanFindingDto(s.getId(), s.getScannedAt(), s.getFilePath(),
                        s.getLineNumber(), s.getRuleMatched(), s.getCommitSha()))
                .toList();
    }
}
