package com.example.booking.repository;

import com.example.booking.entity.ClassSchedule;
import com.example.booking.entity.User;
import com.example.booking.entity.Waitlist;
import com.example.booking.entity.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    /**
     * Finds waitlist entries for a specific class schedule that are currently in 'WAITLISTED' status,
     * ordered by the time they joined in ascending order (FIFO).
     *
     * @param classSchedule The ClassSchedule to find waitlist entries for.
     * @param status The status of the waitlist entry (e.g., WaitlistStatus.WAITLISTED).
     * @return A list of matching Waitlist entries.
     */
    List<Waitlist> findByClassScheduleAndStatusOrderByJoinTimeAsc(ClassSchedule classSchedule, WaitlistStatus status);

    // Check if a user is already on the waitlist for a specific class
    boolean existsByUserAndClassSchedule(User user, ClassSchedule classSchedule);

    List<Waitlist> findByUser(User user);

    // --- METHOD FOR REFUND JOB ---
    /**
     * Finds waitlist entries that are in 'WAITLISTED' status
     * and whose associated class schedule has an end time before the given time.
     * This is ideal for identifying entries that need to be refunded after class ends.
     *
     * @param status The status of the waitlist entry (should be WaitlistStatus.WAITLISTED).
     * @param endTime The LocalDateTime to compare against the class schedule's end time (typically LocalDateTime.now()).
     * @return A list of matching Waitlist entries.
     */
    List<Waitlist> findByStatusAndClassSchedule_EndTimeBefore(WaitlistStatus status, LocalDateTime endTime);

}