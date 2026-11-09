package com.cap48.db;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "drift_findings")
public class DriftFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compared_at", nullable = false)
    private Instant comparedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "env_a_id", nullable = false)
    private Environment envA;

    @ManyToOne(optional = false)
    @JoinColumn(name = "env_b_id", nullable = false)
    private Environment envB;

    @Column(name = "setting_key", nullable = false)
    private String settingKey;

    @Column(name = "value_a")
    private String valueA;

    @Column(name = "value_b")
    private String valueB;

    @Column(nullable = false)
    private boolean allowed;

    public Long getId() {
        return id;
    }

    public Instant getComparedAt() {
        return comparedAt;
    }

    public void setComparedAt(Instant comparedAt) {
        this.comparedAt = comparedAt;
    }

    public Environment getEnvA() {
        return envA;
    }

    public void setEnvA(Environment envA) {
        this.envA = envA;
    }

    public Environment getEnvB() {
        return envB;
    }

    public void setEnvB(Environment envB) {
        this.envB = envB;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getValueA() {
        return valueA;
    }

    public void setValueA(String valueA) {
        this.valueA = valueA;
    }

    public String getValueB() {
        return valueB;
    }

    public void setValueB(String valueB) {
        this.valueB = valueB;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
}
