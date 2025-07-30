package com.example.booking.dto;

import com.example.booking.entity.PackageStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPackageDTO {
    private Long id;
    private Long userId;
    private Long packageId;
    private String packageName;
    private int remainingCredits;
    private LocalDate expiryDate;
    private PackageStatus status; // ACTIVE, EXPIRED, USED_UP
    private Long countryId;
    private String countryName;
}