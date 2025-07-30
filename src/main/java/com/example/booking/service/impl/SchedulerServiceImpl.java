package com.example.booking.service.impl;

import com.example.booking.service.BookingService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class SchedulerServiceImpl {

    private final BookingService bookingService;
    private final RedissonClient redissonClient; // Inject RedissonClient

    public SchedulerServiceImpl(BookingService bookingService, RedissonClient redissonClient) {
        this.bookingService = bookingService;
        this.redissonClient = redissonClient;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Runs every day at 2 AM
    public void runRefundWaitlistCredits() {
        RLock lock = redissonClient.getLock("scheduledTask:refundWaitlistCredits");
        try {
            // Attempt to acquire the lock.
            // tryLock(0, 300, TimeUnit.SECONDS) means:
            //   - waitTime = 0: Don't wait if the lock is already held by another instance.
            //   - leaseTime = 300: Hold the lock for a maximum of 300 seconds (5 minutes)
            //                    if the task takes longer or the instance crashes.
            //                    Redisson's watchdog will extend the lease as long as the task is running.
            if (lock.tryLock(0, 300, TimeUnit.SECONDS)) {
                System.out.println("Running scheduled task: refundWaitlistCreditsAfterClassEnds at " + LocalDateTime.now());
                bookingService.refundWaitlistCreditsAfterClassEnds();
            } else {
                System.out.println("Scheduled task: refundWaitlistCreditsAfterClassEnds - Lock already held by another instance. Skipping.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            System.err.println("Scheduled task interrupted: " + e.getMessage());
        } finally {
            // Ensure the lock is released only if it's currently locked AND held by the current thread
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}