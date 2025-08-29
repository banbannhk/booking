package com.example.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


// ===== CLIENT ERROR EXCEPTIONS (4xx) =====

// 400 Bad Request - Invalid input format/syntax
public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }
}