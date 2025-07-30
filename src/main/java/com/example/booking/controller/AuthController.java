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
        try {
            authServiceImpl.registerUser(registerRequest);
            return new ResponseEntity<>("User registered successfully. Please check your email for verification.", HttpStatus.CREATED);
        } catch (com.example.booking.exception.UserAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authServiceImpl.loginUser(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String token) {
        try {
            authServiceImpl.verifyEmail(email, token);
            return ResponseEntity.ok("Email verified successfully!");
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Email verification failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            authServiceImpl.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok("Password reset link sent to your email if an account exists.");
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Password reset initiation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authServiceImpl.resetPassword(request.getEmail(), request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Password reset failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}