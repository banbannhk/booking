package com.example.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassScheduleDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int requiredCredits;
    private int maxParticipants;
    private int currentParticipants;
    private int availableSlots;
    private Long classInfoId;
    private String classInfoName;
    private String classInfoDescription;
    private Long countryId;
    private String countryName;
    private boolean isFull;
    private boolean isBookedByUser;
    private boolean isOnWaitlistByUser;
}