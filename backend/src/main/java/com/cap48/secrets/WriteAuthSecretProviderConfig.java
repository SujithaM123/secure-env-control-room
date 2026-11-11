package com.cap48.secrets;

import com.cap48.config.SecretsProperties;
import com.cap48.config.WriteAuthProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Picks the {@link SecretProvider} for this environment's write-API key,
 * separate from the DB password ({@link SecretProviderConfig}) and the
 * audit DB password ({@link AuditSecretProviderConfig}). Not marked
 * @Primary — consumers must ask for it by the bean name
 * "writeApiKeySecretProvider".
 */
@Configuration
public class WriteAuthSecretProviderConfig {

    @Bean
    public SecretProvider writeApiKeySecretProvider(SecretsProperties secretsProperties, WriteAuthProperties writeAuthProperties) {
        String provider = secretsProperties.provider();
        return switch (provider) {
            case "file" -> new FileSecretProvider(writeAuthProperties.secretRef());
            case "aws" -> new AwsSecretProvider(SecretsManagerClient.create(), writeAuthProperties.secretRef());
            default -> throw new IllegalStateException(
                    "Unknown secrets.provider '" + provider + "' — expected 'file' or 'aws'");
        };
    }
}
