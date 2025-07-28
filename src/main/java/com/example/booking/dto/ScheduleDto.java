package com.example.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleDto {
    private Long id;
    private String className;
    private String countryCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int requiredCredits;
    private int maxParticipants;
    private int bookedCount;
}
