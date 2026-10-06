package com.example.wtow_transfer.exception;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {
    private final UUID userId;
    private final UUID accountId;

    public AccountNotFoundException(UUID userId, UUID accountId) {
        super("Account not found");
        this.userId = userId;
        this.accountId = accountId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
