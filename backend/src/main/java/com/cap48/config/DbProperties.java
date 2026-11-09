package com.cap48.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db")
public record DbProperties(String url, String username, String secretRef) {
}
