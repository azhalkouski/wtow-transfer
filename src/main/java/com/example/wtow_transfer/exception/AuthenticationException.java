package com.example.wtow_transfer.exception;

public class AuthenticationException extends RuntimeException {
    private final String email;

    public AuthenticationException(String email) {
        super("Authentication failed");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
