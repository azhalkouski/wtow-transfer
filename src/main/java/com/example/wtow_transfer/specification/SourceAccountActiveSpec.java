package com.example.wtow_transfer.specification;

import com.example.wtow_transfer.exception.SourceAccountIsBlockedException;
import com.example.wtow_transfer.exception.TransferException;
import com.example.wtow_transfer.jpa.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

public class SourceAccountActiveSpec implements TransferSpecification {

    @Override
    public boolean isSatisfiedBy(Account source, Account destination, BigDecimal amount) {
        return source.isActive();
    }

    @Override
    public TransferException createException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        return new SourceAccountIsBlockedException(transactionId, sourceAccNumber
                , destinationAccNumber);
    }
}
