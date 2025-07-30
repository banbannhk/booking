package com.example.booking.repository;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.ClassSchedule;
import com.example.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserAndStatus(User user, BookingStatus status);
    boolean existsByUserAndClassScheduleAndStatus(User user, ClassSchedule classSchedule, BookingStatus status);

    // Find active bookings for a user that overlap with a given time range
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = 'BOOKED' " +
            "AND (" +
            "(:newStartTime < b.classSchedule.endTime AND :newEndTime > b.classSchedule.startTime)" +
            ")")
    List<Booking> findOverlappingBookingsForUser(
            @Param("user") User user,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime);

    List<Booking> findByClassScheduleAndStatus(ClassSchedule classSchedule, BookingStatus status);
}