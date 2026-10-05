package com.example.wtow_transfer.jpa.repository;

import com.example.wtow_transfer.jpa.entity.User;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Repository<User, UUID> {
    Optional<User> findByEmail(String email);
}
