package com.cap48.web;

import com.cap48.secrets.SecretProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Gates the 5 write endpoints behind a shared secret, one per environment.
 * Registered on those write paths ({@link WriteAuthFilterConfig}), but a
 * servlet url-pattern mapping is method-agnostic — {@code /api/scan-findings}
 * is also a GET endpoint ({@link ScanFindingsController}) — so this only
 * checks the key on POST; every other method on those same paths passes
 * straight through.
 *
 * <p>The key is fetched from {@link SecretProvider} once, lazily, on the
 * first successful call, and cached in memory for the rest of the app's
 * lifetime — there's no TTL or background refresh. Rotating the secret in
 * Secrets Manager therefore has no effect on a running instance until it's
 * restarted; this is an accepted trade-off given how infrequently this key
 * is expected to change for an out-of-band edit; see {@link #updateCachedKey(String)}
 * for the one case that doesn't need a restart.
 */
public class WriteApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Write-Api-Key";

    private final SecretProvider writeApiKeySecretProvider;
    private volatile String cachedKey;

    public WriteApiKeyFilter(SecretProvider writeApiKeySecretProvider) {
        this.writeApiKeySecretProvider = writeApiKeySecretProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String presented = request.getHeader(HEADER_NAME);
        if (presented == null || !constantTimeEquals(presented, currentKey())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid " + HEADER_NAME);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /** Called by {@link RotationController} right after a successful rotation so this instance checks future requests against the new key with no restart needed. */
    public void updateCachedKey(String newKey) {
        this.cachedKey = newKey;
    }

    /** Fetches the key on first call and caches it; a failed fetch is retried on the next request. */
    private String currentKey() {
        String key = cachedKey;
        if (key == null) {
            synchronized (this) {
                key = cachedKey;
                if (key == null) {
                    key = writeApiKeySecretProvider.get();
                    cachedKey = key;
                }
            }
        }
        return key;
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
