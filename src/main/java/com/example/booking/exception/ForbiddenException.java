// 403 Forbidden - User lacks permission
package com.example.booking.exception;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
}