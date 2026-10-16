package com.example.wtow_transfer.jpa.repository.transferengine;

import com.example.wtow_transfer.jpa.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends Repository<Account, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accNumber IN :accNumbers ORDER BY a.accNumber")
    List<Account> findAllByAccNumberIn(@Param("accNumbers") List<String> accNumbers);

    List<Account> saveAll(Iterable<Account> accounts);
}
