package com.cap48.db;

import jakarta.persistence.*;
import java.time.Instant;

/** A secret-scan hit. No column here ever holds the matched secret value. */
@Entity
@Table(name = "scan_findings")
public class ScanFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "line_number", nullable = false)
    private int lineNumber;

    @Column(name = "rule_matched", nullable = false)
    private String ruleMatched;

    @Column(name = "commit_sha", nullable = false)
    private String commitSha;

    public Long getId() {
        return id;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(Instant scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getRuleMatched() {
        return ruleMatched;
    }

    public void setRuleMatched(String ruleMatched) {
        this.ruleMatched = ruleMatched;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }
}
