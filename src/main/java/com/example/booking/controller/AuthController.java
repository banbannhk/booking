package com.example.booking.controller;

import com.example.booking.dto.request.*;
import com.example.booking.dto.response.LoginResponse;
import com.example.booking.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    public AuthController(AuthServiceImpl authServiceImpl) {
        this.authServiceImpl = authServiceImpl;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authServiceImpl.registerUser(registerRequest);
        return new ResponseEntity<>("User registered successfully. Please check your email for verification.", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authServiceImpl.loginUser(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String token) {
        authServiceImpl.verifyEmail(email, token);
        return ResponseEntity.ok("Email verified successfully!");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authServiceImpl.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("Password reset link sent to your email if an account exists.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authServiceImpl.resetPassword(request.getEmail(), request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password has been reset successfully.");
    }

}