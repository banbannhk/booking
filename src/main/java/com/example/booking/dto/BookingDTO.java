package com.example.booking.dto;

import com.example.booking.entity.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long classScheduleId;
    private String classScheduleName;
    private LocalDateTime classStartTime;
    private LocalDateTime classEndTime;
    private int requiredCredits;
    private BookingStatus status;
    private LocalDateTime bookedAt;
    private LocalDateTime canceledAt;
    private boolean canCancelForRefund;
    private Long countryId;
    private String countryName;
    private boolean waitListed;
    private Long waitListId;
    private String desc;
}