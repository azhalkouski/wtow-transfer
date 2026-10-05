package com.example.wtow_transfer.mapper;

import com.example.wtow_transfer.dto.UserDto;
import com.example.wtow_transfer.jpa.entity.User;

public final class UserMapper {
    private UserMapper() {}

    public static UserDto toDto(User e) {
        return new UserDto(e.getId(), e.getCitizenId(), e.getFirstName(), e.getLastName(),
                e.isActive(), e.getEmail());
    }
}
