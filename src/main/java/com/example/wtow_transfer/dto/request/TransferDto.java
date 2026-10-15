package com.example.wtow_transfer.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Example payload:
 * {
 *     "source": "0000000000000001",
 *     "destination": "0000000000000002",
 *     "amount": 112.0,
 *     "recipientName": "Jack Spiral",
 *     "description": "Money on stuff",
 *     "scheduledFor": "2026-10-14",
 *     "immediate": true
 * }
 */
public record TransferDto(
        @NotNull
        @Pattern(regexp = "\\d{16}")
        String source,
        @NotNull
        @Pattern(regexp = "\\d{16}")
        String destination,
        @NotNull
        @Positive()
        BigDecimal amount,
        @NotBlank
        String recipientName,
        @NotBlank
        String description,
        @NotNull
        LocalDate scheduledFor,
        boolean immediate
) {
}
