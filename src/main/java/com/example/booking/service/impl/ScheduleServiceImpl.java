package com.example.booking.service.impl;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.ClassSchedule;
import com.example.booking.entity.User;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.ClassScheduleRepository;
import com.example.booking.repository.UserRepository;
import com.example.booking.service.ScheduleService;
import jakarta.transaction.Transactional;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<ClassSchedule> getSchedules(Long country) {
        return classScheduleRepository.findByClassInfoCountryId(country);
    }

    @Override
    public Booking bookClass(Long userId, Long scheduleId) {
        String lockKey = "booking_lock_" + scheduleId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                ClassSchedule schedule = classScheduleRepository.findById(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
                User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

                // Check if user has enough credits from their package in this country
                // Check for overlapping bookings (not implemented here)
                // Check maxParticipants limit via booking count

                long currentBookingCount = bookingRepository.countByClassScheduleAndStatus(schedule, "BOOKED");
                if (currentBookingCount >= schedule.getMaxParticipants()) {
                    // Add to waitlist (not implemented here)
                    throw new RuntimeException("Class is full, please join waitlist");
                }

                // Deduct credit from user package (not implemented here)

                Booking booking = new Booking();
                booking.setUser(user);
                booking.setClassSchedule(schedule);
                booking.setStatus(BookingStatus.BOOKED);
                booking.setBookedAt(LocalDateTime.now());

                bookingRepository.save(booking);
                return booking;

            } else {
                throw new RuntimeException("Unable to acquire lock for booking");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Lock interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Override
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStart = booking.getClassSchedule().getStartTime();

        Duration diff = Duration.between(now, classStart);
        if (diff.toHours() >= 4) {
            // Refund credit logic (not implemented here)
        }

        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);

        // Move waitlist user to booked (not implemented here)
    }

    @Override
    public void checkIn(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("User not authorized");
        }
        booking.setStatus(BookingStatus.ATTENDED);
        bookingRepository.save(booking);
    }
}
