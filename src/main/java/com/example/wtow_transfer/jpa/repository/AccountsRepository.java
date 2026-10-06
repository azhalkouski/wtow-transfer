package com.example.wtow_transfer.jpa.repository;

import com.example.wtow_transfer.jpa.entity.Account;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountsRepository extends Repository<Account, UUID> {
    List<Account> findAllByUserId(UUID userId);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
}
