package com.cap48.secrets;

import com.cap48.config.RotateAuthProperties;
import com.cap48.config.SecretsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Picks the {@link SecretProvider} for the rotation endpoint's Basic Auth
 * password - separate from every other secret in this app, since it's a
 * human credential, not a machine-to-machine one. Not marked @Primary —
 * consumers must ask for it by the bean name "rotateAuthPasswordSecretProvider".
 */
@Configuration
public class RotateAuthSecretProviderConfig {

    @Bean
    public SecretProvider rotateAuthPasswordSecretProvider(SecretsProperties secretsProperties, RotateAuthProperties rotateAuthProperties) {
        String provider = secretsProperties.provider();
        return switch (provider) {
            case "file" -> new FileSecretProvider(rotateAuthProperties.passwordSecretRef());
            case "aws" -> new AwsSecretProvider(SecretsManagerClient.create(), rotateAuthProperties.passwordSecretRef());
            default -> throw new IllegalStateException(
                    "Unknown secrets.provider '" + provider + "' — expected 'file' or 'aws'");
        };
    }
}
