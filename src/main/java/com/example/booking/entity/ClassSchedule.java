package com.example.booking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int requiredCredits;

    private int maxParticipants;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_info_id", nullable = false)
    private ClassInfo classInfo;
}

