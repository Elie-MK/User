package com.rakbank.users.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<String> handleUserException(UserException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        StringBuilder message = new StringBuilder("Validation errors: ");
        ex.getConstraintViolations().forEach(violation ->
                message.append(violation.getMessage()).append("; ")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message.toString());
    }

    // Add more exception handlers as needed
}
