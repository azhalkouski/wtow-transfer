package com.example.wtow_transfer.service;

import com.example.wtow_transfer.dto.AccountDto;
import com.example.wtow_transfer.exception.AccountNotFoundException;
import com.example.wtow_transfer.jpa.entity.Account;
import com.example.wtow_transfer.jpa.repository.accountmanager.AccountsRepository;
import com.example.wtow_transfer.mapper.AccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountsService {

    private final AccountsRepository accountsRepository;

    public AccountsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    @Transactional(transactionManager="accountManagerTransactionManager", readOnly = true)
    public List<AccountDto> getAccountsForUser(UUID userId) {
        return accountsRepository.findAllByUserId(userId).stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    @Transactional(transactionManager="accountManagerTransactionManager", readOnly = true)
    public AccountDto getAccountForUser(UUID userId, UUID accountId) {
        Account account = accountsRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException(userId, accountId));
        return AccountMapper.toDto(account);
    }
}
