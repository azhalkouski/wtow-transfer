package com.example.wtow_transfer.specification;

import com.example.wtow_transfer.exception.DestinationAccountIsBlockedException;
import com.example.wtow_transfer.exception.TransferException;
import com.example.wtow_transfer.jpa.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

public class DestinationAccountActiveSpec implements TransferSpecification {

    @Override
    public boolean isSatisfiedBy(Account source, Account destination, BigDecimal amount) {
        return destination.isActive();
    }

    @Override
    public TransferException createException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        return new DestinationAccountIsBlockedException(transactionId, sourceAccNumber
                , destinationAccNumber);
    }
}
