package com.cap48.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Shared by every filter that compares a presented credential against a stored one, so no comparison leaks timing. */
final class ConstantTimeCompare {

    private ConstantTimeCompare() {
    }

    static boolean equals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
