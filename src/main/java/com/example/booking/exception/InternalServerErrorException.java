// 500 Internal Server Error - System issues
package com.example.booking.exception;

public class InternalServerErrorException extends BaseException {
    public InternalServerErrorException(String message) {
        super("INTERNAL_SERVER_ERROR", message);
    }
}