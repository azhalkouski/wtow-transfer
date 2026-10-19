package com.example.wtow_transfer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Failed authentication attempt for email: {}", ex.getEmail());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundException(AccountNotFoundException ex) {
        logger.warn("Failed to find account for userId and accountId: {}, {}", ex.getUserId(),
                ex.getAccountId());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Not found");
    }

    @ExceptionHandler(AccountNumberNotFoundException.class)
    public ProblemDetail handleAccountNumberNotFoundException(AccountNumberNotFoundException ex) {
        this.logTransferException(ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Transfer failed");
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFundsException(InsufficientFundsException ex) {
        this.logTransferException(ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient funds");
    }

    @ExceptionHandler({SourceAccountIsBlockedException.class
            , DestinationAccountIsBlockedException.class})
    public ProblemDetail handleAccountIsBlockedException(TransferException ex) {
        this.logTransferException(ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.EXPECTATION_FAILED, "One of accounts" +
                " is blocked");
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ProblemDetail handleNotAuthenticatedException(NotAuthenticatedException ex) {
        logger.error(ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        logger.error("Unexpected {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logTransferException(TransferException ex) {
        logger.warn("EXCEPTION: {}. Source account Number: {}, destination account number: {}," +
                        " transactionId: {}", ex.getMessage(), ex.getSourceAccNumber()
                , ex.getDestinationAccNumber(), ex.getTransactionId());
    }
}
