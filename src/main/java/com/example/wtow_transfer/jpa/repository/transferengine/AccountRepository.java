package com.example.wtow_transfer.jpa.repository.transferengine;

import com.example.wtow_transfer.jpa.entity.Account;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends Repository<Account, UUID> {

    List<Account> findAllByAccNumberIn(List<String> accNumbers);
    List<Account> saveAll(Iterable<Account> accounts);
}
