package com.cap48.web;

import com.cap48.config.AppProperties;
import com.cap48.db.Environment;
import com.cap48.db.EnvironmentResolver;
import com.cap48.db.RotationEvent;
import com.cap48.db.RotationEventRepository;
import com.cap48.secrets.SecretProvider;
import com.cap48.web.dto.RotationResultDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Rotates this running instance's own write-API key on demand. An instance
 * can only rotate its OWN secret - its IAM role has permission only on its
 * own write-api-key ARN, the same per-environment isolation used everywhere
 * else in this app. {@code env} in the path must match this instance's own
 * APP_ENV; anything else is rejected before ever calling AWS, since a
 * mismatch could never succeed there anyway.
 *
 * Gated by {@link BasicAuthFilter} ({@link RotateAuthFilterConfig}) - a
 * human credential, not the write-API key itself. Unlike every other write
 * endpoint (checked by {@link WriteApiKeyFilter}), rotating the key can't
 * require presenting that same key as the credential to call it from an
 * unauthenticated browser - the browser's native Basic Auth prompt, fired
 * by that filter's 401, is the intended way a person triggers this.
 */
@RestController
public class RotationController {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppProperties appProperties;
    private final SecretProvider writeApiKeySecretProvider;
    private final WriteApiKeyFilter writeApiKeyFilter;
    private final EnvironmentResolver environmentResolver;
    private final RotationEventRepository rotationEvents;

    public RotationController(
            AppProperties appProperties,
            @Qualifier("writeApiKeySecretProvider") SecretProvider writeApiKeySecretProvider,
            WriteApiKeyFilter writeApiKeyFilter,
            EnvironmentResolver environmentResolver,
            RotationEventRepository rotationEvents
    ) {
        this.appProperties = appProperties;
        this.writeApiKeySecretProvider = writeApiKeySecretProvider;
        this.writeApiKeyFilter = writeApiKeyFilter;
        this.environmentResolver = environmentResolver;
        this.rotationEvents = rotationEvents;
    }

    @PostMapping("/api/rotate/write-api-key/{env}")
    public RotationResultDto rotate(@PathVariable String env) {
        if (!env.equals(appProperties.envName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This instance (" + appProperties.envName() + ") cannot rotate another environment's key ("
                            + env + ") - it has no IAM permission to");
        }

        Environment environment = environmentResolver.resolve(env);
        Instant now = Instant.now();
        String oldFingerprint = writeApiKeySecretProvider.fingerprint();
        String newValue = generateToken();
        String newFingerprint = SecretProvider.fingerprintOf(newValue);

        RotationEvent event = new RotationEvent();
        event.setEnvironment(environment);
        event.setStartedAt(now);
        event.setOverlapEndsAt(now);
        event.setOldFingerprint(oldFingerprint);
        event.setNewFingerprint(newFingerprint);
        event.setCommitSha(appProperties.version());

        try {
            writeApiKeySecretProvider.rotate(newValue);
            writeApiKeyFilter.updateCachedKey(newValue);
            event.setStatus("switched");
            event.setCompletedAt(Instant.now());
        } catch (RuntimeException e) {
            event.setStatus("failed");
            rotationEvents.save(event);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Rotation failed: " + e.getMessage());
        }

        rotationEvents.save(event);
        return new RotationResultDto(env, event.getStatus(), oldFingerprint, newFingerprint, event.getCompletedAt());
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
