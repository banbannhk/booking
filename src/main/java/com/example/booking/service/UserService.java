
package com.example.booking.service;

import com.example.booking.dto.CountryDTO;
import com.example.booking.dto.UserDto;
import com.example.booking.entity.Country;
import com.example.booking.entity.User;

public interface UserService {

    User registerUser(UserDto userDto);

    String login(String email, String password);

    User getProfile(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void resetPassword(String email);

    boolean verifyEmail(String token);

    Country registerCountry(CountryDTO countryDTO);
}
