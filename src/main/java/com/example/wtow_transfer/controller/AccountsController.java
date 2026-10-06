package com.example.wtow_transfer.controller;

import com.example.wtow_transfer.dto.AccountDto;
import com.example.wtow_transfer.service.AccountsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

    private static final Logger log = LoggerFactory.getLogger(AccountsController.class);
    private final AccountsService accountService;

    public AccountsController(AccountsService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAccounts() {
        UUID userId = getAuthUserId();
        List<AccountDto> accounts = accountService.getAccountsForUser(userId);
        return ResponseEntity.ok().body(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable UUID id) {
        UUID userId = getAuthUserId();
        AccountDto accountDto;
        try {
            accountDto = accountService.getAccountForUser(userId, id);
        } catch (Exception e) {
            // RestControllerAdvice and custom exceptions will be provided in a followup PR
            log.error("e: ", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(accountDto);
    }

    private static UUID getAuthUserId() {
        return (UUID) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
    }
}
