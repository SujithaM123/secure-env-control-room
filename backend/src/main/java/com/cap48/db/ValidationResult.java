package com.cap48.db;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "validation_results")
public class ValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    /** PASS / FAIL */
    @Column(nullable = false)
    private String status;

    @Column(name = "errors_json", columnDefinition = "json")
    private String errorsJson;

    public Long getId() {
        return id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(Instant checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorsJson() {
        return errorsJson;
    }

    public void setErrorsJson(String errorsJson) {
        this.errorsJson = errorsJson;
    }
}
