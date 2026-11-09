package com.cap48.db;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "deployments")
public class Deployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private Environment environment;

    /** The single promoted build's tag/SHA — same image across Dev, Test and Prod. */
    @Column(name = "image_version", nullable = false)
    private String imageVersion;

    @Column(name = "deployed_at", nullable = false)
    private Instant deployedAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "verification_details_json", columnDefinition = "json")
    private String verificationDetailsJson;

    public Long getId() {
        return id;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public void setImageVersion(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public Instant getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(Instant deployedAt) {
        this.deployedAt = deployedAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationDetailsJson() {
        return verificationDetailsJson;
    }

    public void setVerificationDetailsJson(String verificationDetailsJson) {
        this.verificationDetailsJson = verificationDetailsJson;
    }
}
