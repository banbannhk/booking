package com.example.booking.service.impl;

import com.example.booking.dto.BookingDTO;
import com.example.booking.dto.request.BookingRequest;
import com.example.booking.dto.request.CheckInRequest;
import com.example.booking.dto.WaitlistDTO;
import com.example.booking.entity.*;
import com.example.booking.exception.*;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.ClassScheduleRepository;
import com.example.booking.repository.UserPackageRepository;
import com.example.booking.repository.WaitlistRepository;
import com.example.booking.service.BookingService;
import com.kafka.service.BookingEventPublisher;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserPackageRepository userPackageRepository;
    private final WaitlistRepository waitlistRepository;
    private final RedisServiceImpl redisService;
    private final RedissonClient redissonClient;
    private final BookingEventPublisher eventPublisher;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ClassScheduleRepository classScheduleRepository,
                              UserPackageRepository userPackageRepository,
                              WaitlistRepository waitlistRepository,
                              RedisServiceImpl redisService,
                              RedissonClient redissonClient,
                              BookingEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.userPackageRepository = userPackageRepository;
        this.waitlistRepository = waitlistRepository;
        this.redisService = redisService;
        this.redissonClient = redissonClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public BookingDTO bookClass(User user, BookingRequest request) {
        // Use a distributed lock unique to this class schedule
        RLock lock = redissonClient.getLock("classScheduleLock:" + request.getClassScheduleId());
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS); // Wait 10s, lease 30s
            if (!locked) {
                throw new ConflictException("Could not acquire lock for class booking. Please try again.");
            }

            // --- ALL CRITICAL LOGIC GOES INSIDE THE LOCK ---
            ClassSchedule classSchedule = classScheduleRepository.findById(request.getClassScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class Schedule not found with ID: " + request.getClassScheduleId()));

            // 1. Check if class is in the past
            if (classSchedule.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BusinessRuleViolationException("CLASS_ALREADY_STARTED", "Cannot book a class that has already started or ended.");
            }

            // 2. Check for overlapping bookings
            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookingsForUser(
                    user, classSchedule.getStartTime(), classSchedule.getEndTime());
            if (!overlappingBookings.isEmpty()) {
                throw new ConflictException("You already have an overlapping class booked.");
            }

            // 3. Check if user already booked or on waitlist for this class
            if (bookingRepository.existsByUserAndClassScheduleAndStatus(user, classSchedule, BookingStatus.BOOKED)) {
                throw new ConflictException("You have already booked this class.");
            }
            if (waitlistRepository.existsByUserAndClassSchedule(user, classSchedule)) {
                throw new ConflictException("You are already on the waitlist for this class.");
            }

            // 4. Check available slots using Redis
            Integer currentSlots = redisService.getAvailableSlots(classSchedule.getId());

            Long newSlots = redisService.decrementAvailableSlots(classSchedule.getId());

            // 5. Deduct credits from user's package (Pass class's country for correct package matching)
            UserPackage userPackage = findApplicableUserPackage(user, classSchedule.getRequiredCredits(), classSchedule.getClassInfo().getCountry());
            if (userPackage == null) {
                redisService.incrementAvailableSlots(classSchedule.getId()); // Crucial to revert Redis
                throw new BusinessRuleViolationException("INSUFFICIENT_CREDITS","Not enough credits or no active package available for this class's country.");
            }


            userPackage.setRemainingCredits(userPackage.getRemainingCredits() - classSchedule.getRequiredCredits());
            if (userPackage.getRemainingCredits() <= 0) {
                userPackage.setStatus(PackageStatus.USED_UP);
            }
            userPackageRepository.save(userPackage);

            // If class is full, add to waitlist instead of direct booking
            if (currentSlots == null || currentSlots <= 0) {
                // Class is full, add user to waitlist
                Waitlist waitlistEntry = new Waitlist();
                waitlistEntry.setUser(user);
                waitlistEntry.setClassSchedule(classSchedule);
                waitlistEntry.setJoinTime(LocalDateTime.now());
                waitlistEntry.setStatus(WaitlistStatus.WAITLISTED);
                waitlistEntry.setUserPackage(userPackage);
                waitlistEntry.setCreditsDeductedForWaitlist(classSchedule.getRequiredCredits());

                Waitlist savedWaitlistEntry = waitlistRepository.save(waitlistEntry);

                BookingDTO bookingDTO = new BookingDTO();
                bookingDTO.setWaitListed(true);
                bookingDTO.setWaitListId(savedWaitlistEntry.getId());
                bookingDTO.setClassScheduleId(classSchedule.getId());
                bookingDTO.setUserId(user.getId());
                bookingDTO.setStatus(BookingStatus.WAITLIST);
                bookingDTO.setDesc("Class is full, you've been added to the waitlist.");
                bookingDTO.setRequiredCredits(classSchedule.getRequiredCredits());
                return bookingDTO;
            }


            // 6. Create Booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setClassSchedule(classSchedule);
            booking.setStatus(BookingStatus.BOOKED);
            booking.setBookedAt(LocalDateTime.now());

            Booking savedBooking = bookingRepository.save(booking);

            // After successful booking, publish event
            if (savedBooking.getId() > 0) {
                eventPublisher.publishClassBooked(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        booking.getId(),
                        classSchedule.getClassInfo().getId(),
                        classSchedule.getClassInfo().getName(),
                        classSchedule.getStartTime(),
                        1
                );
            }

            return mapToBookingDTO(savedBooking);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalServerErrorException("Booking operation interrupted.");
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Unexpected error during booking: " + e.getMessage());
            e.printStackTrace();

            // If it's already one of our custom exceptions, rethrow it
            if (e instanceof ResourceNotFoundException || e instanceof BusinessRuleViolationException ||
                    e instanceof ConflictException || e instanceof InternalServerErrorException) {
                throw e;
            }

            // Otherwise, wrap in InternalServerErrorException
            throw new InternalServerErrorException("An unexpected error occurred during booking");
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public BookingDTO cancelBooking(User user, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        ClassSchedule classSchedule = booking.getClassSchedule();
        RLock lock = redissonClient.getLock("classScheduleLock:" + classSchedule.getId());
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new ConflictException("Could not acquire lock for class cancellation. Please try again.");
            }

            // --- ALL CRITICAL LOGIC GOES INSIDE THE LOCK ---
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("You are not authorized to cancel this booking.");
            }

            if (booking.getStatus() != BookingStatus.BOOKED) {
                throw new BusinessRuleViolationException("INVALID_BOOKING_STATUS", "Cannot cancel a booking that is not in BOOKED status.");
            }

            boolean refundEligible = classSchedule.getStartTime().minusHours(4).isAfter(LocalDateTime.now());

            if (refundEligible) {
                UserPackage userPackage = userPackageRepository.findByUserAndPack_Country(user, classSchedule.getClassInfo().getCountry())
                        .stream()
                        .filter(up -> up.getStatus() == PackageStatus.ACTIVE || up.getStatus() == PackageStatus.USED_UP)
                        .findFirst()
                        .orElse(null);

                if (userPackage != null) {
                    userPackage.setRemainingCredits(userPackage.getRemainingCredits() + classSchedule.getRequiredCredits());
                    if (userPackage.getRemainingCredits() > 0 && userPackage.getStatus() == PackageStatus.USED_UP) {
                        userPackage.setStatus(PackageStatus.ACTIVE);
                    }
                    userPackageRepository.save(userPackage);
                } else {
                    System.err.println("Warning: No suitable package found for user " + user.getId() + " to refund credits after cancellation for class " + classSchedule.getId());
                }
            }

            booking.setStatus(BookingStatus.CANCELED);
            booking.setCanceledAt(LocalDateTime.now());
            Booking savedBooking = bookingRepository.save(booking);

            redisService.incrementAvailableSlots(classSchedule.getId());

            processWaitlistAfterCancellation(classSchedule.getId());

            // After cancel booking, publish event
            if (savedBooking.getId() > 0) {
                eventPublisher.publishClassBooked(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        booking.getId(),
                        classSchedule.getClassInfo().getId(),
                        classSchedule.getClassInfo().getName(),
                        classSchedule.getStartTime(),
                        1
                );
            }

            return mapToBookingDTO(savedBooking);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalServerErrorException("Cancellation operation interrupted.");
        } catch (Exception e) {
            System.err.println("Unexpected error during cancellation: " + e.getMessage());
            e.printStackTrace();

            if (e instanceof ResourceNotFoundException || e instanceof BusinessRuleViolationException ||
                    e instanceof ConflictException || e instanceof ForbiddenException ||
                    e instanceof InternalServerErrorException) {
                throw e;
            }

            throw new InternalServerErrorException("An unexpected error occurred during cancellation");
        }finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @Override
    public List<BookingDTO> getMyBookings(User user) {
        try {
            List<Booking> bookings = bookingRepository.findByUserAndStatus(user, BookingStatus.BOOKED);
            return bookings.stream().map(this::mapToBookingDTO).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error retrieving bookings for user " + user.getId() + ": " + e.getMessage());
            throw new InternalServerErrorException("Failed to retrieve your bookings");
        }
    }

    @Override
    public List<WaitlistDTO> getMyWaitlists(User user) {
        try {
            List<Waitlist> waitlists = waitlistRepository.findByUser(user);
            return waitlists.stream().map(this::mapToWaitlistDTO).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error retrieving waitlists for user " + user.getId() + ": " + e.getMessage());
            throw new InternalServerErrorException("Failed to retrieve your waitlists");
        }
    }

    @Override
    @Transactional
    public String checkIn(User user, CheckInRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to check in for this booking.");
        }

        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new BusinessRuleViolationException("INVALID_CHECKIN_STATUS",
                    "Cannot check in for a booking that is not in BOOKED status.");
        }

        ClassSchedule classSchedule = booking.getClassSchedule();
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(classSchedule.getStartTime().minusMinutes(30)) || now.isAfter(classSchedule.getEndTime())) {
            throw new BusinessRuleViolationException("INVALID_CHECKIN_TIME",
                    "Check-in is only allowed 30 minutes before the class starts until the class ends.");
        }

        try {
            booking.setStatus(BookingStatus.ATTENDED);
            bookingRepository.save(booking);
            return "Successfully checked in for class: " + classSchedule.getClassInfo().getName();
        } catch (Exception e) {
            System.err.println("Error during check-in for booking " + request.getBookingId() + ": " + e.getMessage());
            throw new InternalServerErrorException("Failed to check in for the class");
        }
    }


    // --- Internal/Scheduled Methods ---

    @Override
    @Transactional
    public void processWaitlistAfterCancellation(Long classScheduleId) {
        ClassSchedule classSchedule = classScheduleRepository.findById(classScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Class Schedule not found for waitlist processing: " + classScheduleId));

        // Use a distributed lock specific to this class schedule to prevent concurrent promotions
        RLock lock = redissonClient.getLock("classScheduleLock:" + classScheduleId); // Use same lock key as booking for consistency
        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS); // Shorter wait/lease for internal process
            if (!locked) {
                System.err.println("Could not acquire lock for waitlist processing of class " + classScheduleId + ". Another process might be handling it or retrying later.");
                return; // Or throw a specific exception to be caught by a scheduler/retry mechanism
            }

            // Check if there's genuinely an available slot in Redis
            Integer currentAvailableSlots = redisService.getAvailableSlots(classScheduleId);

            // Proceed only if there's at least one slot available
            if (currentAvailableSlots == null || currentAvailableSlots <= 0) {
                System.out.println("No slots available for waitlist processing for class " + classScheduleId + ". Current Redis count: " + (currentAvailableSlots != null ? currentAvailableSlots : "null"));
                return;
            }

            // Get the oldest waitlisted user for this class (FIFO)
            // Ensure findByClassScheduleAndStatusOrderByJoinedAtAsc exists in your WaitlistRepository
            List<Waitlist> waitlistEntries = waitlistRepository.findByClassScheduleAndStatusOrderByJoinTimeAsc(classSchedule, WaitlistStatus.WAITLISTED);

            if (waitlistEntries.isEmpty()) {
                System.out.println("No pending WAITLISTED users found for class " + classScheduleId + " to promote.");
                return; // No one on the waitlist with WAITLISTED status
            }

            Waitlist nextInLine = waitlistEntries.get(0); // Get the first (oldest) waitlisted user
            User waitlistUser = nextInLine.getUser();

            // --- CRITICAL: VERIFY USER ELIGIBILITY AND DEDUCT CREDITS ---
            // Find an applicable UserPackage for the waitlist user.
            // This is where credit availability and country matching are checked at the time of promotion.
            // Assuming classSchedule.getClassInfo().getCountry() exists and provides the country.
            UserPackage userPackageToDeduct = findApplicableUserPackage(waitlistUser, classSchedule.getRequiredCredits(), classSchedule.getClassInfo().getCountry());

            if (userPackageToDeduct == null) {
                System.out.println("Waitlist user " + waitlistUser.getId() + " no longer has an eligible package or sufficient credits for class " + classSchedule.getId() + ". Skipping promotion.");
                // Optionally, mark this waitlist entry as 'PROMOTION_FAILED' to avoid retrying indefinitely
                // (requires adding a new enum status for WaitlistStatus)
                // For now, we just skip and leave it as WAITLISTED, or you might want to remove it entirely
                // or notify the user.
                return;
            }

            // --- Proceed with slot decrement and booking creation if user is eligible ---

            // Deduct slot from Redis first to prevent overbooking from concurrent promotions
            Long newAvailableSlotsAfterDecrement = redisService.decrementAvailableSlots(classSchedule.getId());

            // Double-check for race conditions that might have made slots negative right now
            if (newAvailableSlotsAfterDecrement == null || newAvailableSlotsAfterDecrement < 0) {
                if (newAvailableSlotsAfterDecrement != null) { // If it went negative, increment back
                    redisService.incrementAvailableSlots(classSchedule.getId());
                }
                System.out.println("Class became full during waitlist promotion for " + classSchedule.getId() + ". Slot count: " + newAvailableSlotsAfterDecrement);
                return;
            }

            // --- CREATE THE BOOKING ---
            Booking booking = new Booking();
            booking.setUser(waitlistUser);
            booking.setClassSchedule(classSchedule);
            booking.setStatus(BookingStatus.BOOKED); // User is now confirmed!
            booking.setBookedAt(LocalDateTime.now());
            booking.setUserPackage(userPackageToDeduct);
            bookingRepository.save(booking);
            System.out.println("Booking created for waitlist user " + waitlistUser.getId() + " (promoted from waitlist) for class " + classSchedule.getId());

            // --- UPDATE WAITLIST ENTRY STATUS ---
            nextInLine.setStatus(WaitlistStatus.CONVERTED_TO_BOOKED);
            waitlistRepository.save(nextInLine);
            System.out.println("Waitlist entry for user " + waitlistUser.getId() + " updated to CONVERTED_TO_BOOKED for class " + classSchedule.getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Waitlist processing interrupted for class " + classScheduleId + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during waitlist processing for class " + classScheduleId + ": " + e.getMessage());
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public void refundWaitlistCreditsAfterClassEnds() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Initiating refund process for ended waitlisted classes at: " + now);

        // --- fetch only WAITLISTED entries for ended classes ---
        List<Waitlist> pendingRefundWaitlistEntries =
                waitlistRepository.findByStatusAndClassSchedule_EndTimeBefore(WaitlistStatus.WAITLISTED, now);

        if (pendingRefundWaitlistEntries.isEmpty()) {
            System.out.println("No WAITLISTED entries found for classes that have ended. Exiting refund process.");
            return;
        }

        for (Waitlist waitlist : pendingRefundWaitlistEntries) {
            ClassSchedule classSchedule = waitlist.getClassSchedule(); // Already ensured to be in the past by query
            RLock lock = redissonClient.getLock("classScheduleRefundLock:" + classSchedule.getId());
            try {
                boolean locked = lock.tryLock(1, 5, TimeUnit.SECONDS);
                if (!locked) {
                    System.err.println("Could not acquire lock for refunding waitlist credits for class " + classSchedule.getId() + ". Will skip for now and retry later.");
                    continue;
                }

                UserPackage userPackageToRefundTo = waitlist.getUserPackage();
                Integer creditsToRefund = waitlist.getCreditsDeductedForWaitlist();
                User user = waitlist.getUser();

                if (userPackageToRefundTo != null && creditsToRefund != null && creditsToRefund > 0) {
                    Optional<UserPackage> actualUserPackageOpt = userPackageRepository.findById(userPackageToRefundTo.getId());

                    if (actualUserPackageOpt.isPresent()) {
                        UserPackage actualUserPackage = actualUserPackageOpt.get();
                        actualUserPackage.setRemainingCredits(actualUserPackage.getRemainingCredits() + creditsToRefund);

                        if (actualUserPackage.getStatus() == PackageStatus.USED_UP && actualUserPackage.getRemainingCredits() > 0) {
                            actualUserPackage.setStatus(PackageStatus.ACTIVE);
                        }
                        userPackageRepository.save(actualUserPackage);
                        System.out.println("SUCCESS: Refunded " + creditsToRefund + " credits to user " + user.getId() +
                                " (Package: " + actualUserPackage.getId() + ") for waitlist entry " + waitlist.getId() +
                                " (Class " + classSchedule.getId() + " ended).");

                        waitlist.setStatus(WaitlistStatus.REFUNDED);
                        waitlistRepository.save(waitlist);
                    } else {
                        System.err.println("ERROR: Original UserPackage " + userPackageToRefundTo.getId() + " not found for refunding waitlist entry " + waitlist.getId() + " for user " + user.getId() + ". Waitlist entry status not updated.");
                    }
                } else {
                    System.err.println("WARN: Waitlist entry " + waitlist.getId() + " for user " + user.getId() +
                            " has null/zero creditsToRefund or no associated package for refund. Skipping. Waitlist entry status not updated.");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("ERROR: Refund waitlist credits interrupted for class " + classSchedule.getId() + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("CRITICAL ERROR during refund for class " + classSchedule.getId() + ", waitlist entry " + waitlist.getId() + ": " + e.getMessage());
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        System.out.println("Refund process for ended waitlisted classes completed.");
    }

    private UserPackage findApplicableUserPackage(User user, int requiredCredits, Country targetClassCountry) {
        List<UserPackage> activePackages = userPackageRepository.findByUserAndStatus(user, PackageStatus.ACTIVE);

        return activePackages.stream()
                .filter(up -> up.getRemainingCredits() >= requiredCredits) // Enough credits
                .filter(up -> up.getPack().getCountry().equals(targetClassCountry)) // Package country must match class country
                .findFirst()
                .orElse(null);
    }


    private BookingDTO mapToBookingDTO(Booking booking) {
        ClassSchedule schedule = booking.getClassSchedule();
        boolean canCancelForRefund = schedule.getStartTime().minusHours(4).isAfter(LocalDateTime.now());

        return BookingDTO.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .classScheduleId(schedule.getId())
                .classScheduleName(schedule.getClassInfo().getName())
                .classStartTime(schedule.getStartTime())
                .classEndTime(schedule.getEndTime())
                .requiredCredits(schedule.getRequiredCredits())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .canceledAt(booking.getCanceledAt())
                .canCancelForRefund(canCancelForRefund)
                .countryId(schedule.getClassInfo().getCountry().getId())
                .countryName(schedule.getClassInfo().getCountry().getName())
                .build();
    }

    private WaitlistDTO mapToWaitlistDTO(Waitlist waitlist) {
        ClassSchedule schedule = waitlist.getClassSchedule();
        return WaitlistDTO.builder()
                .id(waitlist.getId())
                .userId(waitlist.getUser().getId())
                .classScheduleId(schedule.getId())
                .classScheduleName(schedule.getClassInfo().getName())
                .classStartTime(schedule.getStartTime())
                .joinedAt(waitlist.getJoinTime())
                .build();
    }
}