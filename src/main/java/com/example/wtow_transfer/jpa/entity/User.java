package com.example.wtow_transfer.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Immutable;

import java.util.UUID;

@Entity
@Immutable
@Table(name = "users", schema = "core")
public class User {

    @Id
    private UUID id;
    private String citizenId;
    private String firstName;
    private String lastName;
    @Column(name = "is_active")
    private boolean active;
    private String email;
    private String passwordHash;

    protected User() {}

    public UUID getId() {
        return id;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isActive() {
        return active;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
