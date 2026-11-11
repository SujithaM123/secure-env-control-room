package com.cap48.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Points at this environment's own write-API key secret — same pattern as
 * {@link DbProperties#secretRef()}, one key per environment, not shared.
 * Pipeline scripts send this key as a header; the backend compares it in
 * {@code WriteApiKeyFilter} before accepting a write to the audit tables.
 */
@ConfigurationProperties(prefix = "write-auth")
public record WriteAuthProperties(String secretRef) {
}
