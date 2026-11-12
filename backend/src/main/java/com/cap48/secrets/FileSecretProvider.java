package com.cap48.secrets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads the secret from a local, git-ignored file. Used only in local
 * development ({@code SECRETS_PROVIDER=file}) so developers never need AWS
 * credentials to run the app.
 */
public class FileSecretProvider implements SecretProvider {

    private final Path secretFilePath;

    public FileSecretProvider(String secretFilePath) {
        this.secretFilePath = Path.of(secretFilePath);
    }

    @Override
    public String get() {
        try {
            return Files.readString(secretFilePath).strip();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not read local secret file at " + secretFilePath, e);
        }
    }

    @Override
    public void rotate(String newValue) {
        try {
            Files.writeString(secretFilePath, newValue);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not write local secret file at " + secretFilePath, e);
        }
    }
}
