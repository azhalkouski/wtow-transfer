package com.example.wtow_transfer.specification;

import com.example.wtow_transfer.exception.InsufficientFundsException;
import com.example.wtow_transfer.exception.TransferException;
import com.example.wtow_transfer.jpa.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

public class SourceHasSufficientFundsSpec implements TransferSpecification {

    @Override
    public boolean isSatisfiedBy(Account source, Account destination, BigDecimal amount) {
        return source.getBalance().compareTo(amount) >= 0;
    }

    @Override
    public TransferException createException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber) {
        return new InsufficientFundsException(transactionId, sourceAccNumber, destinationAccNumber);
    }
}
