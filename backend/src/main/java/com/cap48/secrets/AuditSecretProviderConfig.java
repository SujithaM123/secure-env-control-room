package com.cap48.secrets;

import com.cap48.config.AuditDbProperties;
import com.cap48.config.SecretsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Picks the {@link SecretProvider} for the shared audit database's password,
 * separate from the app's own DB password ({@link SecretProviderConfig}).
 * Not marked @Primary — consumers must ask for it by the bean name
 * "auditSecretProvider".
 */
@Configuration
public class AuditSecretProviderConfig {

    @Bean
    public SecretProvider auditSecretProvider(SecretsProperties secretsProperties, AuditDbProperties auditDbProperties) {
        String provider = secretsProperties.provider();
        return switch (provider) {
            case "file" -> new FileSecretProvider(auditDbProperties.secretRef());
            case "aws" -> new AwsSecretProvider(SecretsManagerClient.create(), auditDbProperties.secretRef());
            default -> throw new IllegalStateException(
                    "Unknown secrets.provider '" + provider + "' — expected 'file' or 'aws'");
        };
    }
}
