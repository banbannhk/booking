package com.example.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "waitlists")
@Data
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "class_schedule_id", nullable = false)
    private ClassSchedule classSchedule;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaitlistStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_package_id", nullable = false)
    private UserPackage userPackage;

    @Column(name = "credits_deducted_for_waitlist", nullable = false)
    private Integer creditsDeductedForWaitlist;
}