package com.example.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull(message = "Class Schedule ID cannot be null")
    private Long classScheduleId;
}