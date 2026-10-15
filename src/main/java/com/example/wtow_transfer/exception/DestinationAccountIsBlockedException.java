package com.example.wtow_transfer.exception;

import java.util.UUID;

public class DestinationAccountIsBlockedException extends TransferException {

    public DestinationAccountIsBlockedException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        super("Destination account is blocked.", transactionId, sourceAccNumber
                , destinationAccNumber);
    }
}
