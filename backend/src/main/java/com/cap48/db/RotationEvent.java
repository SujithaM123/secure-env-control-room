package com.cap48.db;

import jakarta.persistence.*;
import java.time.Instant;

/** old/new fingerprint columns hold only the 8-char sha256 fingerprint, never a secret value. */
@Entity
@Table(name = "rotation_events")
public class RotationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "overlap_ends_at", nullable = false)
    private Instant overlapEndsAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "old_fingerprint", length = 8)
    private String oldFingerprint;

    @Column(name = "new_fingerprint", length = 8)
    private String newFingerprint;

    /** in_progress / switched / rolled_back / failed */
    @Column(nullable = false)
    private String status;

    public Long getId() {
        return id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getOverlapEndsAt() {
        return overlapEndsAt;
    }

    public void setOverlapEndsAt(Instant overlapEndsAt) {
        this.overlapEndsAt = overlapEndsAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getOldFingerprint() {
        return oldFingerprint;
    }

    public void setOldFingerprint(String oldFingerprint) {
        this.oldFingerprint = oldFingerprint;
    }

    public String getNewFingerprint() {
        return newFingerprint;
    }

    public void setNewFingerprint(String newFingerprint) {
        this.newFingerprint = newFingerprint;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
