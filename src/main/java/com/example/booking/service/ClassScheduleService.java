package com.example.booking.service;

import com.example.booking.dto.ClassScheduleDTO;
import com.example.booking.entity.User;

import java.util.List;

public interface ClassScheduleService {
    List<ClassScheduleDTO> getAvailableClassSchedules(Long countryId, User currentUser);
    ClassScheduleDTO getClassScheduleById(Long id, User currentUser); // For external use
}