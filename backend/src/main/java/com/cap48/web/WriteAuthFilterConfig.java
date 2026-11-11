package com.cap48.web;

import com.cap48.secrets.SecretProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link WriteApiKeyFilter} on exactly the 5 write endpoints
 * ({@link WriteController}) — every other path, including all the
 * read-only GET endpoints, is untouched by it.
 */
@Configuration
public class WriteAuthFilterConfig {

    @Bean
    public FilterRegistrationBean<WriteApiKeyFilter> writeApiKeyFilter(
            @Qualifier("writeApiKeySecretProvider") SecretProvider writeApiKeySecretProvider) {
        FilterRegistrationBean<WriteApiKeyFilter> registration =
                new FilterRegistrationBean<>(new WriteApiKeyFilter(writeApiKeySecretProvider));
        registration.addUrlPatterns(
                "/api/scan-findings",
                "/api/validation-results",
                "/api/drift-findings",
                "/api/config-snapshots",
                "/api/deployments");
        return registration;
    }
}
