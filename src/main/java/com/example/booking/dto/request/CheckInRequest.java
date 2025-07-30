package com.example.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInRequest {
    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;
}