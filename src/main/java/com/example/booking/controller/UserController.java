package com.example.booking.controller;

import com.example.booking.dto.UserDTO;
import com.example.booking.dto.request.ChangePasswordRequest;
import com.example.booking.dto.request.UpdateProfileRequest;
import com.example.booking.dto.response.ErrorResponse;
import com.example.booking.entity.User;
import com.example.booking.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final AuthServiceImpl authService;

    public UserController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        try {
            UserDTO updatedUser = authService.updateUserProfile(userDetails.getUsername(), request);
            return ResponseEntity.ok(updatedUser);
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.NOT_FOUND); // User not found (shouldn't happen if authenticated)
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR); // General error
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                 @Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(userDetails.getUsername(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully.");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to change password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}