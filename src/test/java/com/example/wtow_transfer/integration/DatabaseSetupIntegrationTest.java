package com.example.wtow_transfer.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DatabaseSetupIntegrationTest extends TestContainersProvider {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSetupIntegrationTest(@Qualifier("transferEngineDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void shouldHaveSeededAccount() {
        String currentRole = jdbcTemplate.queryForObject("SELECT current_role", String.class);
        assertThat(currentRole).isEqualTo("wtow_transfer_ngn");

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM core.accounts"
                , Integer.class);
        assertThat(count).isEqualTo(3);
    }
}
