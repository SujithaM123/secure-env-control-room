package com.cap48.secrets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Supplies a secret to the code that needs it (a DB password, a write-API
 * key). {@link #get()}'s return value must never be logged, returned from
 * an HTTP endpoint, or put in a response body — only {@link #fingerprint()}
 * is safe for those uses.
 */
public interface SecretProvider {

    /** The raw secret value. For connection/auth use only — never log or expose it. */
    String get();

    /** Overwrites the secret with a new value — rotation. Not every provider needs this; see each implementation. */
    void rotate(String newValue);

    /** sha256(secret), first 8 hex characters — safe to log, store, and display. */
    default String fingerprint() {
        return fingerprintOf(get());
    }

    /** Same algorithm as {@link #fingerprint()}, for a value not already behind a SecretProvider (e.g. a freshly generated rotation candidate). */
    static String fingerprintOf(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
