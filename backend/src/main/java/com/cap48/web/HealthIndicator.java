package com.cap48.web;

import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Extra Actuator health check: is the DB reachable? Reports DOWN with a
 * reason — never with secret content or a raw stack trace that could
 * include it.
 */
@Component("db")
public class HealthIndicator implements org.springframework.boot.actuate.health.HealthIndicator {

    private final DataSource dataSource;

    public HealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return Health.up().build();
            }
            return Health.down().withDetail("reason", "database connection invalid").build();
        } catch (SQLException e) {
            return Health.down().withDetail("reason", "secret unavailable").build();
        }
    }
}
