// 401 Unauthorized - Authentication required
package com.example.booking.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
