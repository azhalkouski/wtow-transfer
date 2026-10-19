package com.example.wtow_transfer.integration;

import org.flywaydb.core.Flyway;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;


public abstract class TestContainersProvider {

    protected static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer("postgres:18-alpine3.23")
                .withDatabaseName("wallet_to_wallet_transfer")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("db/init-test-db.sql");

        POSTGRE_SQL_CONTAINER.start();

        Flyway.configure()
                .dataSource(POSTGRE_SQL_CONTAINER.getJdbcUrl(), "wtow_admin", "admin_pw")
                .schemas("core")
                .locations("classpath:db/migration", "classpath:db/seed")
                .load()
                .migrate();
    }

    @DynamicPropertySource
    static void configureDataSources(DynamicPropertyRegistry registry) {
        String jdbcUrl = POSTGRE_SQL_CONTAINER.getJdbcUrl();
        // user-manager datasource
        registry.add("spring.datasource.user-manager.jdbc-url", () -> jdbcUrl);
        registry.add("spring.datasource.user-manager.username", () -> "wtow_user_mngr");
        registry.add("spring.datasource.user-manager.password", () -> "user_mngr_pw");

        // account-manager datasource
        registry.add("spring.datasource.account-manager.jdbc-url", () -> jdbcUrl);
        registry.add("spring.datasource.account-manager.username", () -> "wtow_account_mngr");
        registry.add("spring.datasource.account-manager.password", () -> "account_mngr_pw");

        // transfer-engine datasource
        registry.add("spring.datasource.transfer-engine.jdbc-url", () -> jdbcUrl);
        registry.add("spring.datasource.transfer-engine.username", () -> "wtow_transfer_ngn");
        registry.add("spring.datasource.transfer-engine.password", () -> "transfer_ngn_pw");
    }

}
