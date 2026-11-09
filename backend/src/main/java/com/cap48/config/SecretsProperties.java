package com.cap48.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "secrets")
public record SecretsProperties(String provider) {
}
