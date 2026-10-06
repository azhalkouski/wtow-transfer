package com.example.wtow_transfer.exception;

public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException() {
        super("No authenticated user in security context – like a bug");
    }
}
