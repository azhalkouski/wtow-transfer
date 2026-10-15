package com.example.wtow_transfer.exception;

import java.util.UUID;

public class TransferException extends RuntimeException {
    private final UUID transactionId;
    private final String sourceAccNumber;
    private final String destinationAccNumber;

    public TransferException(String message, UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        super("Funds transfer failed due to: " + message);
        this.transactionId = transactionId;
        this.sourceAccNumber = sourceAccNumber;
        this.destinationAccNumber = destinationAccNumber;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getSourceAccNumber() {
        return sourceAccNumber;
    }

    public String getDestinationAccNumber() {
        return destinationAccNumber;
    }
}
