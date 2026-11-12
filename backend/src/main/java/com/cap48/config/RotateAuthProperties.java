package com.cap48.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credentials for the rotation endpoint's HTTP Basic Auth - a human
 * credential, not the write-API key itself. username is plain config
 * (non-secret); passwordSecretRef points at a per-environment Secrets
 * Manager entry, same pattern as {@link WriteAuthProperties#secretRef()}.
 */
@ConfigurationProperties(prefix = "rotate-auth")
public record RotateAuthProperties(String username, String passwordSecretRef) {
}
