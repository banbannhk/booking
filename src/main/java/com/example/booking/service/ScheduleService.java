package com.example.booking.service;

import com.example.booking.entity.ClassSchedule;
import com.example.booking.entity.Booking;

import java.util.List;

public interface ScheduleService {

    List<ClassSchedule> getSchedules(Long country);

    Booking bookClass(Long userId, Long scheduleId);

    void cancelBooking(Long bookingId, Long userId);

    void checkIn(Long bookingId, Long userId);

}
