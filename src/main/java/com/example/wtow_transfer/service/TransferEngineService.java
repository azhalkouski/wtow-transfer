package com.example.wtow_transfer.service;

import com.example.wtow_transfer.dto.request.TransferDto;
import com.example.wtow_transfer.exception.AccountNumberNotFoundException;
import com.example.wtow_transfer.jpa.entity.Account;
import com.example.wtow_transfer.jpa.repository.transferengine.AccountRepository;
import com.example.wtow_transfer.specification.DestinationAccountActiveSpec;
import com.example.wtow_transfer.specification.SourceAccountActiveSpec;
import com.example.wtow_transfer.specification.SourceHasSufficientFundsSpec;
import com.example.wtow_transfer.specification.TransferSpecification;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class TransferEngineService {

    private final AccountRepository accountRepository;

    private static final List<TransferSpecification> TRANSFER_SPECS = List.of(
            new SourceHasSufficientFundsSpec(),
            new SourceAccountActiveSpec(),
            new DestinationAccountActiveSpec()
    );

    public TransferEngineService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(transactionManager = "transferEngineTransactionManager")
    public UUID makeTransfer(TransferDto transferDto) {
        // transactionId will be persisted when immutable log is implemented
        // for now it's just a placeholder
        UUID transactionId = UUID.randomUUID();
        String sourceAccNumber = transferDto.source();
        String destinationAccNumber = transferDto.destination();
        BigDecimal amount = transferDto.amount();

        Supplier<? extends RuntimeException> notFoundSupplier = () ->
                new AccountNumberNotFoundException(transactionId, sourceAccNumber
                , destinationAccNumber);

        List<Account> accounts = accountRepository.findAllByAccNumberIn(List.of(sourceAccNumber
                , destinationAccNumber));

        Account source = getAccount(accounts, sourceAccNumber, notFoundSupplier);
        Account destination = getAccount(accounts, destinationAccNumber, notFoundSupplier);

        for (TransferSpecification spec : TRANSFER_SPECS) {
            if (!spec.isSatisfiedBy(source, destination, amount)) {
                throw spec.createException(transactionId, sourceAccNumber, destinationAccNumber);
            }
        }

        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        accountRepository.saveAll(List.of(source, destination));

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
