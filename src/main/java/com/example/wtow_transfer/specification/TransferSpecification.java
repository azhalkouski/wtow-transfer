package com.example.wtow_transfer.specification;

import com.example.wtow_transfer.exception.TransferException;
import com.example.wtow_transfer.jpa.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferSpecification {

    boolean isSatisfiedBy(Account source, Account destination, BigDecimal amount);
    TransferException createException(UUID transactionId, String sourceAccNumber
            , String destinationAccNumber);
}
