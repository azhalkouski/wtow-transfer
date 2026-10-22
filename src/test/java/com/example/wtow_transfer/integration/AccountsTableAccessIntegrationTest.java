package com.example.wtow_transfer.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
public class AccountsTableAccessIntegrationTest extends TestContainersProvider {

    private static final String USER_EMAIL_FROM_SEED = "bill_turner@gmail.com";

    private static final String SELECT_STATEMENT_NAME = "SELECT";
    private static final String INSERT_STATEMENT_NAME = "INSERT";
    private static final String UPDATE_STATEMENT_NAME = "UPDATE";
    private static final String DELETE_STATEMENT_NAME = "DELETE";
    private static final String TRUNCATE_STATEMENT_NAME = "TRUNCATE";

    private static final String SCHEMA_NAME = "core";
    private static final String ACCOUNTS_TABLE_NAME = "accounts";
    private static final String USER_MANAGER_ROLE_NAME = "wtow_user_mngr";
    private static final String ACCOUNT_MANAGER_ROLE_NAME = "wtow_account_mngr";
    private static final String TRANSFER_ENGINE_ROLE_NAME = "wtow_transfer_ngn";

    private final JdbcTemplate userManagerJdbc;
    private final JdbcTemplate accountManagerJdbc;
    private final JdbcTemplate transferEngineJdbc;

    public AccountsTableAccessIntegrationTest(
            @Qualifier("userManagerDataSource") DataSource userManager,
            @Qualifier("accountManagerDataSource") DataSource accountManager,
            @Qualifier("transferEngineDataSource") DataSource transferEngine) {
        this.userManagerJdbc = new JdbcTemplate(userManager);
        this.accountManagerJdbc = new JdbcTemplate(accountManager);
        this.transferEngineJdbc = new JdbcTemplate(transferEngine);
    }

    private List<String> columnsExposedToPrivilegeType(JdbcTemplate jdbcTemplate
            , String table, String role, String privilegeType) {
        return jdbcTemplate.queryForList("""
                SELECT column_name
                FROM information_schema.column_privileges
                WHERE table_schema = ?
                    AND table_name = ? AND grantee = ? AND privilege_type = ?
                """,
                String.class, SCHEMA_NAME, table, role, privilegeType);
    }


    @Test
    void tableHasRlsEnabled() {
        Boolean rlsEnabled = transferEngineJdbc.queryForObject(
                """
                SELECT relrowsecurity
                FROM pg_class c
                JOIN pg_namespace n ON c.relnamespace = n.oid
                WHERE c.relname = 'accounts'
                  AND n.nspname = 'core'
                """,
                Boolean.class
        );

        assertThat(rlsEnabled).isTrue();
    }

    @Nested
    class UserManagerRole {

        private List<String> extractColumnsExposedTo(String statementName) {
            return columnsExposedToPrivilegeType(userManagerJdbc, ACCOUNTS_TABLE_NAME
                    , USER_MANAGER_ROLE_NAME, statementName);
        }

        @Test
        void selectPermissionDenied() {
            List<String> columns = extractColumnsExposedTo(SELECT_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

        @Test
        void insertPermissionDenied() {
            List<String> columns = extractColumnsExposedTo(INSERT_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

        @Test
        void updatePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(UPDATE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

        @Test
        void deletePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(DELETE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

        @Test
        void truncatePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(TRUNCATE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

    }


    @Nested
    class AccountManagerRole {

        private List<String> extractColumnsExposedTo(String statementName) {
            return columnsExposedToPrivilegeType(accountManagerJdbc, ACCOUNTS_TABLE_NAME
                    , ACCOUNT_MANAGER_ROLE_NAME, statementName);
        }

        @Test
        void selectPermissionGrantedOnAllColumns() {
            List<String> columns = extractColumnsExposedTo(SELECT_STATEMENT_NAME);
            assertThat(columns.size()).isEqualTo(6);
            assertThat(columns.containsAll(List.of("id", "user_id", "acc_number", "balance"
                    , "currency_id", "is_active"))).isTrue();
        }

        @Test
        void userCanReadOwnAccountsPolicyIsInPlace() {
            Map<String, Object> policy = accountManagerJdbc.queryForMap(
                    """
                    SELECT cmd, roles::text, qual
                    FROM pg_policies
                    WHERE schemaname = 'core'
                      AND tablename = 'accounts'
                      AND policyname = 'user_can_read_own_accounts'
                    """
            );

            assertThat(policy.get("cmd")).isEqualTo(SELECT_STATEMENT_NAME);
            assertThat(policy.get("roles").toString()).contains(ACCOUNT_MANAGER_ROLE_NAME);
            assertThat(policy.get("qual").toString()).contains("app.current_user_id");
        }

        @Test
        void readsRecordsOfCurrentUser_RlsPolicyBehaviourCheck() {
            UUID currentUserId = userManagerJdbc.queryForObject("SELECT id FROM core.get_user_for_auth(?)"
                    , UUID.class, USER_EMAIL_FROM_SEED);

            assertThat(currentUserId).isNotNull();

            String setCurrUserStatement = "SELECT set_config('app.current_user_id', '" + currentUserId + "', true)";
            String selectUserIdFromFirstAccount = "SELECT user_id FROM core.accounts LIMIT 1";

            UUID userIdFromAccount = accountManagerJdbc.execute(
                    (ConnectionCallback<UUID>) conn -> {
                        boolean originalAutoCommit = conn.getAutoCommit();
                        try {
                            conn.setAutoCommit(false);
                            try (Statement stmt = conn.createStatement()) {
                                stmt.execute(setCurrUserStatement);
                                try (ResultSet rs = stmt.executeQuery(selectUserIdFromFirstAccount)) {
                                    return rs.next() ? UUID.fromString(rs.getString(1)) : null;
                                }
                            }
                        } finally {
                            conn.rollback();
                            conn.setAutoCommit(originalAutoCommit);
                        }
                    }
            );

            assertThat(userIdFromAccount).isEqualTo(currentUserId);
        }

        @Test
        void insertPermissionGrantedOnAllColumns() {
            List<String> columns = extractColumnsExposedTo(INSERT_STATEMENT_NAME);
            assertThat(columns.size()).isEqualTo(6);
            assertThat(columns.containsAll(List.of("id", "user_id", "acc_number", "balance"
                    , "currency_id", "is_active"))).isTrue();
        }

        @Test
        void userCanInsertOwnAccountsPolicyIsInPlace() {
            Map<String, Object> policy = accountManagerJdbc.queryForMap(
                    """
                    SELECT cmd, roles::text, with_check
                    FROM pg_policies
                    WHERE schemaname = 'core'
                      AND tablename = 'accounts'
                      AND policyname = 'user_can_insert_own_accounts'
                    """
            );

            assertThat(policy.get("cmd")).isEqualTo(INSERT_STATEMENT_NAME);
            assertThat(policy.get("roles").toString()).contains(ACCOUNT_MANAGER_ROLE_NAME);
            assertThat(policy.get("with_check").toString()).contains("app.current_user_id");
        }

        @Test
        void insertsRecordOfCurrentUser_RlsPolicyBehaviourCheck() {
            UUID currentUserId = userManagerJdbc.queryForObject("SELECT id FROM core.get_user_for_auth(?)"
                    , UUID.class, USER_EMAIL_FROM_SEED);

            assertThat(currentUserId).isNotNull();

            String setCurrUserStatement = "SELECT set_config('app.current_user_id', '" + currentUserId + "', true)";
            String insertAccount = """
                    INSERT INTO core.accounts (user_id, balance, currency_id, acc_number) VALUES
                (
                    ?::uuid, 100000.00,
                    (SELECT id FROM core.currencies WHERE code = 'EUR'), '0000000000000004'
                ) RETURNING user_id
                """;

            UUID userIdFromAccount = accountManagerJdbc.execute(
                    (ConnectionCallback<UUID>) conn -> {
                        boolean originalAutoCommit = conn.getAutoCommit();
                        try {
                            conn.setAutoCommit(false);
                            try (Statement stmt = conn.createStatement()) {
                                stmt.execute(setCurrUserStatement);
                                try (PreparedStatement ps = conn.prepareStatement(insertAccount)) {
                                    ps.setString(1, currentUserId.toString());
                                    try (ResultSet rs = ps.executeQuery()) {
                                        return rs.next() ? UUID.fromString(rs.getString(1)) : null;
                                    }
                                }
                            }
                        } finally {
                            conn.rollback();
                            conn.setAutoCommit(originalAutoCommit);
                        }
                    }
            );

            assertThat(userIdFromAccount).isEqualTo(currentUserId);
        }

        @Test
        void updatePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(UPDATE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

        @Test
        void deletePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(DELETE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

        @Test
        void truncatePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(TRUNCATE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

    }

    @Nested
    class TransferEngineRole {

        private List<String> extractColumnsExposedTo(String statementName) {
            return columnsExposedToPrivilegeType(transferEngineJdbc, ACCOUNTS_TABLE_NAME
                    , TRANSFER_ENGINE_ROLE_NAME, statementName);
        }

        @Test
        void selectPermissionGrantedOnAllColumns() {
            List<String> columns = extractColumnsExposedTo(SELECT_STATEMENT_NAME);
            assertThat(columns.size()).isEqualTo(6);
            assertThat(columns.containsAll(List.of("id", "user_id", "acc_number", "balance"
                    , "currency_id", "is_active"))).isTrue();
        }

        @Test
        void canReadOwnAccountsPolicyIsInPlace() {
            Map<String, Object> policy = transferEngineJdbc.queryForMap(
                    """
                    SELECT cmd, roles::text, qual
                    FROM pg_policies
                    WHERE schemaname = 'core'
                      AND tablename = 'accounts'
                      AND policyname = 'transfer_ngn_can_read_all'
                    """
            );

            assertThat(policy.get("cmd")).isEqualTo(SELECT_STATEMENT_NAME);
            assertThat(policy.get("roles").toString()).contains(TRANSFER_ENGINE_ROLE_NAME);
            assertThat(policy.get("qual").toString()).isEqualTo("true");
        }

        @Test
        void readsAccounts_RlsPolicyBehaviourCheck() {
            BigDecimal balance = transferEngineJdbc.queryForObject(
                    "SELECT balance FROM core.accounts LIMIT 1", BigDecimal.class);
            assertThat(balance).isNotNull();
        }

        @Test
        void updatePermissionGrantedOnBalanceColumnOnly() {
            List<String> columns = extractColumnsExposedTo(UPDATE_STATEMENT_NAME);
            assertThat(columns.size()).isOne();
            assertThat(columns.getFirst()).isEqualTo("balance");
        }

        @Test
        void canUpdateAllPolicyIsInPlace() {
            Map<String, Object> policy = transferEngineJdbc.queryForMap(
                    """
                    SELECT cmd, roles::text, qual, with_check
                    FROM pg_policies
                    WHERE schemaname = 'core'
                      AND tablename = 'accounts'
                      AND policyname = 'transfer_ngn_can_update_all'
                    """
            );

            assertThat(policy.get("cmd")).isEqualTo(UPDATE_STATEMENT_NAME);
            assertThat(policy.get("roles").toString()).contains(TRANSFER_ENGINE_ROLE_NAME);
            assertThat(policy.get("qual").toString()).isEqualTo("true");
            assertThat(policy.get("with_check").toString()).isEqualTo("true");
        }

        @Test
        @Transactional("transferEngineTransactionManager")
        void updatesBalance_RlsPolicyBehaviourCheck() {
            Integer countOfUpdates = transferEngineJdbc
                    .update("UPDATE core.accounts SET balance = 20000");
            assertThat(countOfUpdates).isGreaterThan(0);
        }

        @Test
        void insertPermissionDenied() {
            List<String> columns = extractColumnsExposedTo(INSERT_STATEMENT_NAME);
            assertThat(columns.size()).isZero();

        }

        @Test
        void deletePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(DELETE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

        @Test
        void truncatePermissionDenied() {
            List<String> columns = extractColumnsExposedTo(TRUNCATE_STATEMENT_NAME);
            assertThat(columns.size()).isZero();
        }

    }
}
