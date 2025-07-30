package com.example.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageDTO {
    private Long id;
    private String name;
    private int credits;
    private BigDecimal price;
    private int expiryDays;
    private LocalDateTime createdAt;
    private Long countryId;
    private String countryName;
    private String countryCode;
}