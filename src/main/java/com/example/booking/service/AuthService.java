package com.example.booking.service;

import com.example.booking.dto.UserDTO;
import com.example.booking.dto.request.LoginRequest;
import com.example.booking.dto.request.RegisterRequest;
import com.example.booking.dto.request.UpdateProfileRequest;
import com.example.booking.dto.response.LoginResponse;
import com.example.booking.entity.User;

public interface AuthService {

    User registerUser(RegisterRequest registerRequest);

    LoginResponse loginUser(LoginRequest loginRequest);

    void verifyEmail(String email, String token);

    void changePassword(String email, String oldPassword, String newPassword);

    void initiatePasswordReset(String email);

    void resetPassword(String email, String token, String newPassword);

    User getUserProfile(String email);

    UserDTO getUserByEmail(String email);

    UserDTO updateUserProfile(String email, UpdateProfileRequest updateRequest);
}