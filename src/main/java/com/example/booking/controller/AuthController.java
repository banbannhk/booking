package com.example.booking.controller;

import com.example.booking.dto.request.*;
import com.example.booking.dto.response.LoginResponse;
import com.example.booking.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountLockedException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    public AuthController(AuthServiceImpl authServiceImpl) {
        this.authServiceImpl = authServiceImpl;
    }

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authServiceImpl.registerUser(registerRequest);
        return new ResponseEntity<>("User registered successfully. Please check your email for verification.", HttpStatus.CREATED);
    }


    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }

//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//        LoginResponse loginResponse = authServiceImpl.loginUser(loginRequest);
//        return ResponseEntity.ok(loginResponse);
//    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        // Set up logging context - this will be available even in @ControllerAdvice
        MDC.put("email", loginRequest.getEmail());
        MDC.put("action", "user_login");
        MDC.put("endpoint", "/login");
//        MDC.put("ipAddress", getClientIpAddress(request));
        MDC.put("userAgent", request.getHeader("User-Agent"));
        MDC.put("startTime", String.valueOf(startTime));

        try {
            logger.info("Login attempt started for user: {}", loginRequest.getEmail());

            // Your existing service call - exceptions go to @ControllerAdvice
            LoginResponse loginResponse = authServiceImpl.loginUser(loginRequest);

            // Only log SUCCESS here since @ControllerAdvice handles failures
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("userId", loginRequest.getEmail().toString());
            MDC.put("loginStatus", "success");
            MDC.put("duration", String.valueOf(duration));

            logger.info("Login successful for user: {} (ID: {}) in {}ms",
                    loginRequest.getEmail(),
                    loginRequest.getEmail(),
                    duration);

            return ResponseEntity.ok(loginResponse);

        } finally {
            // Clean up MDC - but keep some context for @ControllerAdvice
            // Only remove controller-specific data, keep email, action, etc.
            MDC.remove("userId");
            MDC.remove("loginStatus");
            MDC.remove("duration");
        }
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