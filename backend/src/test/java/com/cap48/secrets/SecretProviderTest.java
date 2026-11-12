package com.cap48.secrets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretProviderTest {

    @Test
    void fingerprintOfIsEightHexCharsAndDeterministic() {
        String fingerprint = SecretProvider.fingerprintOf("some-secret-value");

        assertThat(fingerprint).hasSize(8);
        assertThat(fingerprint).matches("[0-9a-f]{8}");
        assertThat(SecretProvider.fingerprintOf("some-secret-value")).isEqualTo(fingerprint);
    }

    @Test
    void fingerprintOfDiffersForDifferentValues() {
        assertThat(SecretProvider.fingerprintOf("value-a"))
                .isNotEqualTo(SecretProvider.fingerprintOf("value-b"));
    }

    @Test
    void fileSecretProviderRotateThenGetReturnsTheNewValue(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws Exception {
        java.nio.file.Path secretFile = tempDir.resolve("secret.txt");
        java.nio.file.Files.writeString(secretFile, "original-value");
        FileSecretProvider provider = new FileSecretProvider(secretFile.toString());

        assertThat(provider.get()).isEqualTo("original-value");

        provider.rotate("rotated-value");

        assertThat(provider.get()).isEqualTo("rotated-value");
    }
}
