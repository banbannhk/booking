// 409 Conflict - Resource state conflicts
package com.example.booking.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}