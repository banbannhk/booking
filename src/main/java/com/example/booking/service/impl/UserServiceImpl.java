package com.example.booking.service.impl;

import com.example.booking.dto.CountryDTO;
import com.example.booking.dto.UserDto;
import com.example.booking.entity.Country;
import com.example.booking.entity.User;
import com.example.booking.repository.CountryRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.UserService;
import com.example.booking.util.EmailUtil;
import com.example.booking.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public User registerUser(UserDto userDto) {
        // Add logic to check if email exists, encrypt password, save user, send verification email (mock)
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setVerified(false);
        userRepository.save(user);

        // mock send verify email
        emailUtil.sendVerifyEmail(user.getEmail());

        return user;
    }

    @Override
    public String login(String email, String password) {
        // authenticate and generate JWT
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
//        if (!user.isVerified()) {
//            throw new RuntimeException("Email not verified");
//        }
        return jwtUtil.generateToken(user.getEmail());
    }

    @Override
    public User getProfile(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void resetPassword(String email) {
        // Mock reset password logic - e.g., send reset link
        emailUtil.sendVerifyEmail(email);
    }

    @Override
    public boolean verifyEmail(String token) {
        // mock verification logic, decode token, activate user
        // For now always return true
        return true;
    }

    @Override
    public Country registerCountry(CountryDTO countryDTO) {
        Country c = new Country();
        c.setCode(countryDTO.getCode());
        c.setName(countryDTO.getName());
        countryRepository.save(c);
        return c;
    }
}
