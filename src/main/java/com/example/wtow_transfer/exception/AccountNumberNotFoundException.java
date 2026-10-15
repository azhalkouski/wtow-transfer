package com.example.wtow_transfer.exception;

import java.util.UUID;

public class AccountNumberNotFoundException extends TransferException {

    public AccountNumberNotFoundException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        super("Account not found by accNumber", transactionId, sourceAccNumber
                , destinationAccNumber);
    }
}
