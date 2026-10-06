package com.example.wtow_transfer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(
        UUID id,
        UUID userId,
        BigDecimal balance,
        CurrencyDto currency
) { }
