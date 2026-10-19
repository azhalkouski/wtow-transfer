package com.example.wtow_transfer.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConcurrentTransferIntegrationTest extends TestContainersProvider {

    private static final int THREAD_POOL_SIZE = 10;
    private static final int COUNT_500 = 500;
    private static final int COUNT_1000 = 1000;

    private static final String SELECT_BALANCE_BY_ACC_NUMBER =
            "SELECT balance FROM core.accounts WHERE acc_number = ?";

    @LocalServerPort
    private int port;

    private final JdbcTemplate transferEngineJdbcTemplate;
    private RestTestClient restTestClient;

    private final Map<String, BigDecimal> initialAccountBalances = new HashMap<>();

    private static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("100.00");

    private static final String BILL_EMAIL = "bill_turner@gmail.com";
    private static final String BILL_PASSWORD = "123";
    private static final String BILL_ACC_NUMBER = "0000000000000001";

    private static final String JACK_EMAIL = "jack_spiral@gmail.com";
    private static final String JACK_PASSWORD = "123123";
    private static final String JACK_ACC_NUMBER = "0000000000000002";

    private static final String ALICE_EMAIL = "alice_mystery@gmail.com";
    private static final String ALICE_PASSWORD = "123123123";
    private static final String ALICE_ACC_NUMBER = "0000000000000003";

    public ConcurrentTransferIntegrationTest(
            @Qualifier("transferEngineDataSource") DataSource transferEngineDataSource) {
        this.transferEngineJdbcTemplate = new JdbcTemplate(transferEngineDataSource);
    }

    @BeforeAll
    void captureInitialBalances() {
        List<Map<String, Object>> rows = transferEngineJdbcTemplate.queryForList(
                "SELECT acc_number, balance FROM core.accounts"
        );

        for (Map<String, Object> accountBalanceRow : rows) {
            String accNumber = (String) accountBalanceRow.get("acc_number");
            // PostgreSQL numeric comes as BigDecimal
            BigDecimal balance = (BigDecimal) accountBalanceRow.get("balance");
            initialAccountBalances.put(accNumber, balance);
        }
    }

    @BeforeEach
    void createRestTestClient() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @BeforeEach
    void resetBalances() {
        for (Map.Entry<String, BigDecimal> item : initialAccountBalances.entrySet()) {
            transferEngineJdbcTemplate.update(
                    "UPDATE core.accounts SET balance = ? WHERE acc_number = ?",
                    item.getValue(), item.getKey()
            );
        }
    }

    @Test
    void concurrentTransfersPreserveMoneyConservation() {
        BigDecimal totalBefore = transferEngineJdbcTemplate.queryForObject(
                "SELECT SUM(balance) FROM core.accounts",
                BigDecimal.class
        );

        String billSessionId = login(BILL_EMAIL, BILL_PASSWORD);
        String jackSessionId = login(JACK_EMAIL, JACK_PASSWORD);
        String aliceSessionId = login(ALICE_EMAIL, ALICE_PASSWORD);

        Map<String, String> billToJackTransferBody = buildImmediateTransferBody(BILL_ACC_NUMBER,
                JACK_ACC_NUMBER, TRANSFER_AMOUNT, "Jack");
        Map<String, String> jackToBillTransferBody = buildImmediateTransferBody(JACK_ACC_NUMBER,
                BILL_ACC_NUMBER, TRANSFER_AMOUNT, "Bill");
        Map<String, String> aliceToBillTransferBody = buildImmediateTransferBody(ALICE_ACC_NUMBER,
                BILL_ACC_NUMBER, TRANSFER_AMOUNT, "Bill");
        Map<String, String> aliceToJackTransferBody = buildImmediateTransferBody(ALICE_ACC_NUMBER,
                JACK_ACC_NUMBER, TRANSFER_AMOUNT, "Jack");

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            Runnable billToJackTransfer = () -> postTransfer(billSessionId, billToJackTransferBody);
            Runnable jackToBillTransfer = () -> postTransfer(jackSessionId, jackToBillTransferBody);
            Runnable aliceToBillTransfer = () -> postTransfer(aliceSessionId, aliceToBillTransferBody);
            Runnable aliceToJackTransfer = () -> postTransfer(aliceSessionId, aliceToJackTransferBody);

            scheduleTransfers(executor, futures, COUNT_1000, billToJackTransfer);
            scheduleTransfers(executor, futures, COUNT_1000, jackToBillTransfer);
            scheduleTransfers(executor, futures, COUNT_500, aliceToBillTransfer);
            scheduleTransfers(executor, futures, COUNT_500, aliceToJackTransfer);

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        BigDecimal billBalance = transferEngineJdbcTemplate.queryForObject(
                SELECT_BALANCE_BY_ACC_NUMBER, BigDecimal.class, BILL_ACC_NUMBER);
        BigDecimal jackBalance = transferEngineJdbcTemplate.queryForObject(
                SELECT_BALANCE_BY_ACC_NUMBER, BigDecimal.class, JACK_ACC_NUMBER);
        BigDecimal aliceBalance = transferEngineJdbcTemplate.queryForObject(
                SELECT_BALANCE_BY_ACC_NUMBER, BigDecimal.class, ALICE_ACC_NUMBER);

        assertThat(billBalance).isEqualByComparingTo(BigDecimal.valueOf(150000.00));
        assertThat(jackBalance).isEqualByComparingTo(BigDecimal.valueOf(150000.00));
        assertThat(aliceBalance).isEqualByComparingTo(BigDecimal.valueOf(0.00));

        BigDecimal totalAfter = billBalance.add(jackBalance).add(aliceBalance);
        assertThat(totalAfter).isEqualByComparingTo(totalBefore);
    }

    @Test
    void balancesAreResetBeforeEachTest() {
        BigDecimal billBalance = transferEngineJdbcTemplate.queryForObject(
                SELECT_BALANCE_BY_ACC_NUMBER, BigDecimal.class, BILL_ACC_NUMBER);
        BigDecimal jackBalance = transferEngineJdbcTemplate.queryForObject(
                SELECT_BALANCE_BY_ACC_NUMBER, BigDecimal.class, JACK_ACC_NUMBER);
        BigDecimal aliceBalance = transferEngineJdbcTemplate.queryForObject(
                SELECT_BALANCE_BY_ACC_NUMBER, BigDecimal.class, ALICE_ACC_NUMBER);

        assertThat(billBalance).isEqualByComparingTo(BigDecimal.valueOf(100000.00));
        assertThat(jackBalance).isEqualByComparingTo(BigDecimal.valueOf(100000.00));
        assertThat(aliceBalance).isEqualByComparingTo(BigDecimal.valueOf(100000.00));

        BigDecimal total = billBalance.add(jackBalance).add(aliceBalance);
        assertThat(total).isEqualByComparingTo(new BigDecimal("300000.00"));
    }

    private String login(String email, String password) {
        Map<String, String> loginReqBody = Map.of("email", email, "password", password);
        RestTestClient.ResponseSpec spec = restTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginReqBody)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("JSESSIONID")
                .expectCookie().httpOnly("JSESSIONID", true);

        return Objects.requireNonNull(
                spec.returnResult().getResponseCookies().getFirst("JSESSIONID")
        ).getValue();
    }

    private Map<String, String> buildImmediateTransferBody(String sourceAccNumber
            , String destinationAccNumber, BigDecimal amount, String recipientName) {
        return Map.of(
                "source", sourceAccNumber,
                "destination", destinationAccNumber,
                "amount", amount.toString(),
                "recipientName", recipientName,
                "description", "Test transfer",
                "scheduledFor", LocalDate.now().toString(),
                "immediate", "true"
        );
    }

    private void postTransfer(String sessionId, Map<String, String> transferBody) {
        restTestClient.post()
                .uri("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("JSESSIONID", sessionId)
                .body(transferBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("transactionId").isNotEmpty();
    }


    private void scheduleTransfers(ExecutorService executor, List<CompletableFuture<Void>> futures
            , int count, Runnable task) {
        for (int i = 0; i < count; i++) {
            futures.add(CompletableFuture.runAsync(task, executor));
        }
    }
}
