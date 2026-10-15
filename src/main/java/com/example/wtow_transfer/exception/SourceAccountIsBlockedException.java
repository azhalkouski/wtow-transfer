package com.example.wtow_transfer.exception;

import java.util.UUID;

public class SourceAccountIsBlockedException extends TransferException {

    public SourceAccountIsBlockedException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        super("Source account is blocked.", transactionId, sourceAccNumber
                , destinationAccNumber);
    }
}
