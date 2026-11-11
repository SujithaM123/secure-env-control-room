package com.cap48.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection details for the shared cap48_audit database — the same
 * database/user across Dev, Test and Prod, distinct from each environment's
 * own isolated database (see {@link DbProperties}).
 */
@ConfigurationProperties(prefix = "audit-db")
public record AuditDbProperties(String url, String username, String secretRef) {
}
