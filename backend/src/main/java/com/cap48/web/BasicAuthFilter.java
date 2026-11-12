package com.cap48.web;

import com.cap48.secrets.SecretProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Gates the rotation endpoint with HTTP Basic Auth - a human credential,
 * deliberately decoupled from the write-API key itself (which
 * {@link WriteApiKeyFilter} checks for every other write endpoint). Rotating
 * the write-API key is the one write action a person, not a pipeline
 * script, triggers from the browser, and the browser's native Basic Auth
 * prompt (fired by the 401 + WWW-Authenticate below) is exactly the UI for
 * that - the real credential never has to be embedded in frontend code.
 *
 * Like {@link WriteApiKeyFilter}, the password is fetched from
 * {@link SecretProvider} once, lazily, and cached for the app's lifetime.
 */
public class BasicAuthFilter extends OncePerRequestFilter {

    private static final String REALM = "CAP-48 Rotation";

    private final String username;
    private final SecretProvider passwordSecretProvider;
    private volatile String cachedPassword;

    public BasicAuthFilter(String username, SecretProvider passwordSecretProvider) {
        this.username = username;
        this.passwordSecretProvider = passwordSecretProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        Credentials presented = header == null ? null : parse(header);

        if (presented == null
                || !ConstantTimeCompare.equals(presented.username(), username)
                || !ConstantTimeCompare.equals(presented.password(), currentPassword())) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid credentials");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String currentPassword() {
        String password = cachedPassword;
        if (password == null) {
            synchronized (this) {
                password = cachedPassword;
                if (password == null) {
                    password = passwordSecretProvider.get();
                    cachedPassword = password;
                }
            }
        }
        return password;
    }

    private static Credentials parse(String header) {
        if (!header.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
            int colon = decoded.indexOf(':');
            if (colon < 0) {
                return null;
            }
            return new Credentials(decoded.substring(0, colon), decoded.substring(colon + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private record Credentials(String username, String password) {
    }
}
