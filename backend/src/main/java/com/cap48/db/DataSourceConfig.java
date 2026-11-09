package com.cap48.db;

import com.cap48.config.DbProperties;
import com.cap48.secrets.SecretProvider;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Builds the DB connection pool. The password is fetched from
 * {@link SecretProvider#get()} only here, used immediately to configure the
 * pool, and not retained anywhere else.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(DbProperties dbProperties, SecretProvider secretProvider) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dbProperties.url());
        dataSource.setUsername(dbProperties.username());
        dataSource.setPassword(secretProvider.get());
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }
}
