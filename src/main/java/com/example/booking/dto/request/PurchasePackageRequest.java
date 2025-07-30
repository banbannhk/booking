package com.example.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchasePackageRequest {
    @NotNull(message = "Package ID cannot be null")
    private Long packageId;
}