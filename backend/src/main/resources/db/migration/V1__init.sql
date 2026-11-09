CREATE TABLE environments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE config_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    captured_at TIMESTAMP NOT NULL,
    settings_json JSON NOT NULL,
    CONSTRAINT fk_config_snapshots_environment FOREIGN KEY (environment_id) REFERENCES environments (id)
);

CREATE TABLE validation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL,
    errors_json JSON,
    CONSTRAINT fk_validation_results_environment FOREIGN KEY (environment_id) REFERENCES environments (id)
);

CREATE TABLE drift_findings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compared_at TIMESTAMP NOT NULL,
    env_a_id BIGINT NOT NULL,
    env_b_id BIGINT NOT NULL,
    setting_key VARCHAR(255) NOT NULL,
    value_a TEXT,
    value_b TEXT,
    allowed BOOLEAN NOT NULL,
    CONSTRAINT fk_drift_findings_env_a FOREIGN KEY (env_a_id) REFERENCES environments (id),
    CONSTRAINT fk_drift_findings_env_b FOREIGN KEY (env_b_id) REFERENCES environments (id)
);

CREATE TABLE scan_findings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scanned_at TIMESTAMP NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    line_number INT NOT NULL,
    rule_matched VARCHAR(255) NOT NULL,
    commit_sha VARCHAR(64) NOT NULL
);

CREATE TABLE rotation_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    overlap_ends_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    old_fingerprint VARCHAR(8),
    new_fingerprint VARCHAR(8),
    status VARCHAR(16) NOT NULL,
    CONSTRAINT fk_rotation_events_environment FOREIGN KEY (environment_id) REFERENCES environments (id)
);

CREATE TABLE deployments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    image_version VARCHAR(128) NOT NULL,
    deployed_at TIMESTAMP NOT NULL,
    verified BOOLEAN NOT NULL,
    verification_details_json JSON,
    CONSTRAINT fk_deployments_environment FOREIGN KEY (environment_id) REFERENCES environments (id)
);

CREATE TABLE access_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    iam_role VARCHAR(128) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    justification VARCHAR(512) NOT NULL,
    CONSTRAINT fk_access_audit_environment FOREIGN KEY (environment_id) REFERENCES environments (id)
);
