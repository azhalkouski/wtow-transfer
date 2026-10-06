package com.example.wtow_transfer.mapper;

import com.example.wtow_transfer.dto.AccountDto;
import com.example.wtow_transfer.dto.CurrencyDto;
import com.example.wtow_transfer.jpa.entity.Account;
import com.example.wtow_transfer.jpa.entity.CurrencyEntity;

public class AccountMapper {
    private AccountMapper() {}

    public static AccountDto toDto(Account e) {
        CurrencyEntity currencyEntity = e.getCurrency();
        CurrencyDto currencyDto = new CurrencyDto(currencyEntity.getId(), currencyEntity.getCode(),
                currencyEntity.getDescription());
        return new AccountDto(e.getId(), e.getUserId(), e.getBalance(), currencyDto);
    }
}
