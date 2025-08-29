package com.example.booking.controller;

import com.example.booking.dto.ClassScheduleDTO;
import com.example.booking.entity.User;
import com.example.booking.service.AuthService;
import com.example.booking.service.ClassScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class-schedules")
public class ClassScheduleController extends Thread{

    private final ClassScheduleService classScheduleService;
    private final AuthService authService;

    public ClassScheduleController(ClassScheduleService classScheduleService, AuthService authService) {
        this.classScheduleService = classScheduleService;
        this.authService = authService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<ClassScheduleDTO>> getAvailableClassSchedules(
            @RequestParam Long countryId,
            @AuthenticationPrincipal(expression = "username") String userEmail // Get email directly
    ) {
        User currentUser = null;
        if (userEmail != null) {
            currentUser = authService.getUserProfile(userEmail);
        }
        List<ClassScheduleDTO> schedules = classScheduleService.getAvailableClassSchedules(countryId, currentUser);
        return ResponseEntity.ok(schedules);
    }

    // Get a single class schedule details
    @GetMapping("/{id}")
    public ResponseEntity<ClassScheduleDTO> getClassScheduleDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "username") String userEmail
    ) {
        User currentUser = null;
        if (userEmail != null) {
            currentUser = authService.getUserProfile(userEmail);
        }
        ClassScheduleDTO schedule = classScheduleService.getClassScheduleById(id, currentUser);
        return ResponseEntity.ok(schedule);
    }

}