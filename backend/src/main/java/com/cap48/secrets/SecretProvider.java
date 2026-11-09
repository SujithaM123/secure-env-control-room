package com.cap48.secrets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Supplies the database secret to the code that opens the DB connection.
 * {@link #get()}'s return value must never be logged, returned from an HTTP
 * endpoint, or put in a response body — only {@link #fingerprint()} is safe
 * for those uses.
 */
public interface SecretProvider {

    /** The raw secret value. For DB connection use only — never log or expose it. */
    String get();

    /** sha256(secret), first 8 hex characters — safe to log, store, and display. */
    default String fingerprint() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(get().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
