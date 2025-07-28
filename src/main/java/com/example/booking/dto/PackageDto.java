package com.example.booking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PackageDto {
    private Long id;
    private String name;
    private String countryCode;
    private int credits;
    private double price;
    private LocalDate expiryDate;
    private boolean expired;
}
