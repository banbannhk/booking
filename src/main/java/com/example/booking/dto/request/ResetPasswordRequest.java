package com.example.booking.dto.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String token; // For verification
    private String newPassword;
}
