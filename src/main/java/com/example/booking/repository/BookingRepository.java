package com.example.booking.repository;

import com.example.booking.entity.Booking;
import com.example.booking.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByClassScheduleAndStatus(ClassSchedule classSchedule, String status);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByClassScheduleId(Long classScheduleId);

    // Add any additional queries needed for waitlist, booking status, etc.
}
