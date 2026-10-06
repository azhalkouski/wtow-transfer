package com.example.wtow_transfer.controller;

import com.example.wtow_transfer.dto.AccountDto;
import com.example.wtow_transfer.service.AccountsService;
import com.example.wtow_transfer.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

    private final AccountsService accountService;
    private final AuthService authService;

    public AccountsController(AccountsService accountService, AuthService authService) {
        this.accountService = accountService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<AccountDto>> getAccounts() {
        UUID userId = authService.getAuthenticatedUserId();
        List<AccountDto> accounts = accountService.getAccountsForUser(userId);
        return ResponseEntity.ok().body(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable UUID id) {
        UUID userId = authService.getAuthenticatedUserId();
        AccountDto accountDto = accountService.getAccountForUser(userId, id);
        return ResponseEntity.ok().body(accountDto);
    }
}
