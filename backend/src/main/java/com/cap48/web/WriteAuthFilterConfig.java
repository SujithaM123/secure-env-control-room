package com.cap48.web;

import com.cap48.secrets.SecretProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link WriteApiKeyFilter} on the 5 pipeline-driven write
 * endpoints ({@link WriteController}) — every other path, including all
 * the read-only GET endpoints and the rotation endpoint
 * ({@link RotationController}, gated separately by
 * {@link BasicAuthFilter} - see {@link RotateAuthFilterConfig}), is
 * untouched by it. The filter is its own bean (not built inline inside the
 * registration) so {@link RotationController} can be handed the same
 * instance and update its cached key immediately after a successful
 * rotation.
 */
@Configuration
public class WriteAuthFilterConfig {

    @Bean
    public WriteApiKeyFilter writeApiKeyFilter(
            @Qualifier("writeApiKeySecretProvider") SecretProvider writeApiKeySecretProvider) {
        return new WriteApiKeyFilter(writeApiKeySecretProvider);
    }

    @Bean
    public FilterRegistrationBean<WriteApiKeyFilter> writeApiKeyFilterRegistration(WriteApiKeyFilter writeApiKeyFilter) {
        FilterRegistrationBean<WriteApiKeyFilter> registration = new FilterRegistrationBean<>(writeApiKeyFilter);
        registration.addUrlPatterns(
                "/api/scan-findings",
                "/api/validation-results",
                "/api/drift-findings",
                "/api/config-snapshots",
                "/api/deployments");
        return registration;
    }
}
