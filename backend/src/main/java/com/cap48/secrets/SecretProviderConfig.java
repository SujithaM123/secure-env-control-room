package com.cap48.secrets;

import com.cap48.config.DbProperties;
import com.cap48.config.SecretsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Picks the {@link SecretProvider} implementation from {@code secrets.provider}:
 * "file" locally, "aws" in Dev/Test/Prod. No AWS credentials are configured
 * here — the SDK's default credential provider chain resolves them from the
 * environment's IAM role. Marked @Primary so existing unqualified injections
 * (the app's own DB password, config-info fingerprint) keep resolving here
 * now that a second SecretProvider bean exists for the audit DB password.
 */
@Configuration
public class SecretProviderConfig {

    @Bean
    @Primary
    public SecretProvider secretProvider(SecretsProperties secretsProperties, DbProperties dbProperties) {
        String provider = secretsProperties.provider();
        return switch (provider) {
            case "file" -> new FileSecretProvider(dbProperties.secretRef());
            case "aws" -> new AwsSecretProvider(SecretsManagerClient.create(), dbProperties.secretRef());
            default -> throw new IllegalStateException(
                    "Unknown secrets.provider '" + provider + "' — expected 'file' or 'aws'");
        };
    }
}
