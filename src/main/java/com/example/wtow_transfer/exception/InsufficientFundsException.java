package com.example.wtow_transfer.exception;

import java.util.UUID;

public class InsufficientFundsException extends TransferException {

    public InsufficientFundsException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        super("Insufficient funds", transactionId, sourceAccNumber, destinationAccNumber);
    }
}
