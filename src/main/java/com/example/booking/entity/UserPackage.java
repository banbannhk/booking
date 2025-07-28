package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package pack;

    private int remainingCredits;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private PackageStatus status;
}
