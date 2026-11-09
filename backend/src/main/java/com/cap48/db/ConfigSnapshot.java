package com.cap48.db;

import jakarta.persistence.*;
import java.time.Instant;

/** A point-in-time copy of an environment's non-secret settings — never a password. */
@Entity
@Table(name = "config_snapshots")
public class ConfigSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "settings_json", nullable = false, columnDefinition = "json")
    private String settingsJson;

    public Long getId() {
        return id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Instant capturedAt) {
        this.capturedAt = capturedAt;
    }

    public String getSettingsJson() {
        return settingsJson;
    }

    public void setSettingsJson(String settingsJson) {
        this.settingsJson = settingsJson;
    }
}
