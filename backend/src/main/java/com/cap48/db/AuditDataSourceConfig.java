package com.cap48.db;

import com.cap48.config.AuditDbProperties;
import com.cap48.secrets.SecretProvider;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Connects to the shared cap48_audit database — the same database/user
 * reachable from Dev, Test and Prod alike — that holds the 8 cross-
 * environment audit tables (environments, config_snapshots,
 * validation_results, drift_findings, scan_findings, rotation_events,
 * deployments, access_audit). This is deliberately separate from each
 * environment's own isolated database ({@link DataSourceConfig}): that one
 * stays siloed per environment; this one is shared so the Compare/History
 * pages can see all three environments at once.
 *
 * Marked @Primary because every JPA entity/repository in this app belongs
 * to the audit schema — Spring Boot's Flyway and JPA auto-configuration
 * resolve to whichever DataSource bean is primary when more than one
 * exists, so this is what migrations and repositories target automatically.
 */
@Configuration
public class AuditDataSourceConfig {

    @Bean
    @Primary
    public DataSource auditDataSource(AuditDbProperties auditDbProperties,
                                       @Qualifier("auditSecretProvider") SecretProvider auditSecretProvider) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(auditDbProperties.url());
        dataSource.setUsername(auditDbProperties.username());
        dataSource.setPassword(auditSecretProvider.get());
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }
}
