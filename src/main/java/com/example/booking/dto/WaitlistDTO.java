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
public class WaitlistDTO {
    private Long id;
    private Long userId;
    private Long classScheduleId;
    private String classScheduleName;
    private LocalDateTime classStartTime;
    private LocalDateTime joinedAt;
}