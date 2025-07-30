package com.example.booking.service.impl;

import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl {

    public boolean sendVerificationEmail(String recipientEmail, String verificationLink) {
        // Mock implementation for sending verification email
        System.out.println("Mock Email Service: Sending verification email to " + recipientEmail);
        System.out.println("Verification Link: " + verificationLink);
        return true;
    }

    public boolean sendPasswordResetEmail(String recipientEmail, String resetLink) {
        System.out.println("Mock Email Service: Sending password reset email to " + recipientEmail);
        System.out.println("Reset Link: " + resetLink);
        // Simulate success or failure
        return true;
    }
}
