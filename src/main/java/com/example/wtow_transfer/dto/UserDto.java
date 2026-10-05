package com.example.wtow_transfer.dto;

import java.util.UUID;

public record UserDto(
        UUID userId,
        String citizenId,
        String firstName,
        String lastName,
        boolean active,
        String email
) { }
