package com.example.booking.exception;

// One exception class for all business rules
public class BusinessRuleViolationException extends BaseException {
    public BusinessRuleViolationException(String errorCode, String message) {
        super(errorCode, message);
    }
}