package com.example.booking.controller;

import com.example.booking.entity.Booking;
import com.example.booking.entity.ClassSchedule;
import com.example.booking.service.ScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/list")
    public ResponseEntity<List<ClassSchedule>> getSchedules(@RequestParam Long country) {
        return ResponseEntity.ok(scheduleService.getSchedules(country));
    }

    @PostMapping("/book")
    public ResponseEntity<Booking> bookClass(@RequestParam Long userId, @RequestParam Long scheduleId) {
        return ResponseEntity.ok(scheduleService.bookClass(userId, scheduleId));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelBooking(@RequestParam Long bookingId, @RequestParam Long userId) {
        scheduleService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-in")
    public ResponseEntity<Void> checkIn(@RequestParam Long bookingId, @RequestParam Long userId) {
        scheduleService.checkIn(bookingId, userId);
        return ResponseEntity.ok().build();
    }
}
