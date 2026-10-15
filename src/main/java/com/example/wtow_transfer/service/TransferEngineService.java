package com.example.wtow_transfer.service;

import com.example.wtow_transfer.dto.request.TransferDto;
import com.example.wtow_transfer.exception.AccountNumberNotFoundException;
import com.example.wtow_transfer.exception.DestinationAccountIsBlockedException;
import com.example.wtow_transfer.exception.InsufficientFundsException;
import com.example.wtow_transfer.exception.SourceAccountIsBlockedException;
import com.example.wtow_transfer.jpa.entity.Account;
import com.example.wtow_transfer.jpa.repository.transferengine.AccountRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class TransferEngineService {

    private final AccountRepository accountRepository;

    public TransferEngineService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(transactionManager = "transferEngineTransactionManager")
    public UUID makeTransfer(TransferDto transferDto) {
        // transactionId will be persisted when immutable log is implemented
        // for now it's just a placeholder
        UUID transactionId = UUID.randomUUID();
        Supplier<? extends RuntimeException> notFoundSupplier = () ->
                new AccountNumberNotFoundException(transactionId, transferDto.source()
                , transferDto.destination());

        List<Account> accounts = accountRepository.findAllByAccNumberIn(List.of(transferDto.source()
                , transferDto.destination()));

        Account source = getAccount(accounts, transferDto.source(), notFoundSupplier);
        Account destination = getAccount(accounts, transferDto.destination(), notFoundSupplier);

        if (source.getBalance().compareTo(transferDto.amount()) >= 0) {
            if (source.isActive()) {
                if (destination.isActive()) {
                    source.setBalance(source.getBalance().subtract(transferDto.amount()));
                    destination.setBalance(destination.getBalance().add(transferDto.amount()));
                    accountRepository.saveAll(List.of(source, destination));

                } else {
                    throw new DestinationAccountIsBlockedException(transactionId
                            , transferDto.source(), destination.getAccNumber());
                }

            } else {
                throw new SourceAccountIsBlockedException(transactionId, source.getAccNumber()
                        , destination.getAccNumber());
            }
        } else {
            throw new InsufficientFundsException(transactionId, transferDto.source()
                    , destination.getAccNumber());
        }
        return transactionId;
    }

    private static @NonNull Account getAccount(List<Account> accounts, String transferDto
            , Supplier<? extends RuntimeException> notFoundExceptionSupplier) {
        return accounts.stream()
                .filter(account -> account.getAccNumber().equals(transferDto))
                .findFirst()
                .orElseThrow(notFoundExceptionSupplier);
    }
}
