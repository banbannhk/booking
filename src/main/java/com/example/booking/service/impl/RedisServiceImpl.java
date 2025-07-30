package com.example.booking.service.impl;

import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.ClassSchedule;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.ClassScheduleRepository;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisServiceImpl {

    private static final String CLASS_SLOTS_PREFIX = "class:slots:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ClassScheduleRepository classScheduleRepository;
    private final BookingRepository bookingRepository; // Inject BookingRepository

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate,
                        ClassScheduleRepository classScheduleRepository,
                        BookingRepository bookingRepository) { // Add BookingRepository to constructor
        this.redisTemplate = redisTemplate;
        this.classScheduleRepository = classScheduleRepository;
        this.bookingRepository = bookingRepository; // Assign it
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initializeAllClassScheduleSlotsOnStartup(ContextRefreshedEvent event) {
        System.out.println("Initializing all class schedule slots in Redis on application startup...");
        List<ClassSchedule> classSchedules = classScheduleRepository.findAll();
        for (ClassSchedule schedule : classSchedules) {
            // Calculate actual available slots based on existing bookings in the DB
            long currentBookedParticipants = bookingRepository.findByClassScheduleAndStatus(schedule, BookingStatus.BOOKED).size();
            int actualAvailableSlots = schedule.getMaxParticipants() - (int) currentBookedParticipants;

            // Ensure available slots don't go negative (shouldn't if logic is correct, but for safety)
            if (actualAvailableSlots < 0) {
                actualAvailableSlots = 0;
            }

            // Set the calculated available slots for each class
            setInitialClassScheduleSlots(schedule.getId(), actualAvailableSlots);
        }
        System.out.println("Finished initializing " + classSchedules.size() + " class schedule slots.");
    }

    /**
     * Sets the initial available slots for a specific class schedule in Redis.
     * This is useful when a new class schedule is created or its capacity is updated.
     *
     * @param classScheduleId The ID of the class schedule.
     * @param capacity The maximum capacity (initial slots) for the class.
     */
    public void setInitialClassScheduleSlots(Long classScheduleId, int capacity) {
        String key = CLASS_SLOTS_PREFIX + classScheduleId;
        redisTemplate.opsForValue().set(key, capacity);
        System.out.println("Redis: Initialized slots for class " + classScheduleId + " to " + capacity);
    }

    public Long decrementAvailableSlots(Long classScheduleId) {
        String key = CLASS_SLOTS_PREFIX + classScheduleId;
        Long remaining = redisTemplate.opsForValue().decrement(key);
        System.out.println("Redis: Decremented slots for class " + classScheduleId + ". Remaining: " + remaining);
        return remaining;
    }

    public Long incrementAvailableSlots(Long classScheduleId) {
        String key = CLASS_SLOTS_PREFIX + classScheduleId;
        Long remaining = redisTemplate.opsForValue().increment(key);
        System.out.println("Redis: Incremented slots for class " + classScheduleId + ". Remaining: " + remaining);
        return remaining;
    }

    public Integer getAvailableSlots(Long classScheduleId) {
        String key = CLASS_SLOTS_PREFIX + classScheduleId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public void deleteClassScheduleSlots(Long classScheduleId) {
        String key = CLASS_SLOTS_PREFIX + classScheduleId;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            System.out.println("Redis: Deleted slots for class " + classScheduleId);
        } else {
            System.out.println("Redis: No slots found or deleted for class " + classScheduleId);
        }
    }
}