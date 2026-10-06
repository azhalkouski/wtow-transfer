package com.example.wtow_transfer.service;

import com.example.wtow_transfer.dto.AccountDto;
import com.example.wtow_transfer.jpa.entity.Account;
import com.example.wtow_transfer.jpa.repository.AccountsRepository;
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

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsForUser(UUID userId) {
        return accountsRepository.findAllByUserId(userId).stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountForUser(UUID userId, UUID accountId) throws Exception {
        // RestControllerAdvice and custom exceptions will be provided in a followup PR
        Account account = accountsRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new Exception("Not found"));
        return AccountMapper.toDto(account);
    }
}
