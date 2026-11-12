package com.cap48.web;

import com.cap48.config.RotateAuthProperties;
import com.cap48.secrets.SecretProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link BasicAuthFilter} on exactly the rotation endpoint
 * ({@link RotationController}) - the one write action a person triggers
 * from the browser, authenticated with a human credential rather than the
 * write-API key the other write endpoints check.
 */
@Configuration
public class RotateAuthFilterConfig {

    @Bean
    public BasicAuthFilter rotateBasicAuthFilter(
            RotateAuthProperties rotateAuthProperties,
            @Qualifier("rotateAuthPasswordSecretProvider") SecretProvider rotateAuthPasswordSecretProvider) {
        return new BasicAuthFilter(rotateAuthProperties.username(), rotateAuthPasswordSecretProvider);
    }

    @Bean
    public FilterRegistrationBean<BasicAuthFilter> rotateBasicAuthFilterRegistration(BasicAuthFilter rotateBasicAuthFilter) {
        FilterRegistrationBean<BasicAuthFilter> registration = new FilterRegistrationBean<>(rotateBasicAuthFilter);
        registration.addUrlPatterns("/api/rotate/write-api-key/*");
        return registration;
    }
}
