package com.cap48.secrets;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

/**
 * Reads the secret from AWS Secrets Manager. Used in Dev/Test/Prod
 * ({@code SECRETS_PROVIDER=aws}). The secret ARN/name comes from
 * {@code DB_SECRET_REF} — a pointer, never the value itself. AWS credentials
 * are resolved by the SDK's default credential chain (IAM role) — none are
 * configured here.
 */
public class AwsSecretProvider implements SecretProvider {

    private final SecretsManagerClient client;
    private final String secretRef;

    public AwsSecretProvider(SecretsManagerClient client, String secretRef) {
        this.client = client;
        this.secretRef = secretRef;
    }

    @Override
    public String get() {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretRef)
                    .build();
            GetSecretValueResponse response = client.getSecretValue(request);
            return response.secretString();
        } catch (SecretsManagerException e) {
            throw new IllegalStateException("secret unavailable", e);
        }
    }
}
