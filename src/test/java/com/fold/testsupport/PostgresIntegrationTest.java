package com.fold.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Shared PostgreSQL integration-test infrastructure.
 *
 * <p>The PostgreSQL container is started once for the lifetime of the test
 * JVM and shared by all integration tests that extend this class.</p>
 *
 * <p>This lifecycle is intentional. Spring may cache and reuse application
 * contexts between test classes. Restarting the database container between
 * subclasses would therefore leave a cached DataSource pointing at a stale
 * mapped port.</p>
 */
public abstract class PostgresIntegrationTest {

    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:17-alpine"
            );

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void postgresProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
    }
}