package com.cap48.db;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Looks up an environment row by name, creating it on first use. The write
 * endpoints are the only thing that ever populates {@code environments} —
 * nothing seeds it ahead of time, so the first write for "dev"/"test"/"prod"
 * is what creates that row.
 */
@Service
public class EnvironmentResolver {

    private static final List<String> KNOWN_NAMES = List.of("dev", "test", "prod");

    private final EnvironmentRepository environments;

    public EnvironmentResolver(EnvironmentRepository environments) {
        this.environments = environments;
    }

    public Environment resolve(String name) {
        if (!KNOWN_NAMES.contains(name)) {
            throw new IllegalArgumentException("Unknown environment: " + name);
        }
        return environments.findByName(name).orElseGet(() -> {
            Environment env = new Environment();
            env.setName(name);
            env.setCreatedAt(Instant.now());
            return environments.save(env);
        });
    }
}
