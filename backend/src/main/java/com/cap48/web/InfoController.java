package com.cap48.web;

import com.cap48.config.AppProperties;
import com.cap48.secrets.SecretProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Proves which environment and secret version are running, without ever
 * exposing the secret value itself — only its fingerprint.
 */
@RestController
public class InfoController {

    private final AppProperties appProperties;
    private final SecretProvider secretProvider;

    public InfoController(AppProperties appProperties, SecretProvider secretProvider) {
        this.appProperties = appProperties;
        this.secretProvider = secretProvider;
    }

    @GetMapping("/api/config-info")
    public Map<String, String> configInfo() {
        return Map.of(
                "env", appProperties.envName(),
                "version", appProperties.version(),
                "secretFingerprint", secretProvider.fingerprint()
        );
    }
}
