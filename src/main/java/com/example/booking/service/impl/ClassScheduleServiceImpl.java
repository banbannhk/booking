package com.example.booking.service.impl;

import com.example.booking.dto.ClassScheduleDTO;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.ClassSchedule;
import com.example.booking.entity.Country;
import com.example.booking.entity.User;
import com.example.booking.exception.InternalServerErrorException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.ClassScheduleRepository;
import com.example.booking.repository.WaitlistRepository;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.service.ClassScheduleService;
import com.example.booking.service.CountryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassScheduleServiceImpl implements ClassScheduleService {

    private final ClassScheduleRepository classScheduleRepository;
    private final CountryService countryService;
    private final RedisServiceImpl redisService;
    private final BookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;


    public ClassScheduleServiceImpl(ClassScheduleRepository classScheduleRepository,
                                    CountryService countryService,
                                    RedisServiceImpl redisService,
                                    BookingRepository bookingRepository,
                                    WaitlistRepository waitlistRepository) {
        this.classScheduleRepository = classScheduleRepository;
        this.countryService = countryService;
        this.redisService = redisService;
        this.bookingRepository = bookingRepository;
        this.waitlistRepository = waitlistRepository;
    }

    @Override
    public List<ClassScheduleDTO> getAvailableClassSchedules(Long countryId, User currentUser) {

      try{
            Country country = countryService.getCountryById(countryId);
            List<ClassSchedule> schedules = classScheduleRepository.findByClassInfoCountryIdOrderByStartTimeAsc(country.getId());

            return schedules.stream()
                    .filter(schedule -> schedule.getStartTime().isAfter(LocalDateTime.now()))
                    .map(schedule -> mapToClassScheduleDTO(schedule, currentUser))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }

            System.err.println("Error retrieving class schedules for country " + countryId + ": " + e.getMessage());
            throw new InternalServerErrorException("Failed to retrieve class schedules");
        }
    }

    @Override
    public ClassScheduleDTO getClassScheduleById(Long id, User currentUser) {

        try{

            ClassSchedule schedule = classScheduleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Class Schedule not found with ID: " + id));
            return mapToClassScheduleDTO(schedule, currentUser);

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new InternalServerErrorException("Failed to retrieve class schedule");
        }
    }

    private ClassScheduleDTO mapToClassScheduleDTO(ClassSchedule schedule, User currentUser) {
        Integer currentAvailableSlots = redisService.getAvailableSlots(schedule.getId());
        System.out.println("Current Available Slots from redis " + currentAvailableSlots);
        if (currentAvailableSlots == null) {
            long currentBookedParticipants = bookingRepository.findByClassScheduleAndStatus(schedule, BookingStatus.BOOKED).size();
            System.out.println("currentBookedParticipants " + currentBookedParticipants);
            currentAvailableSlots = schedule.getMaxParticipants() - (int) currentBookedParticipants;
            System.out.println("currentAvailableSlots " + currentAvailableSlots);
            if (currentAvailableSlots < 0) currentAvailableSlots = 0; // Should not happen if logic is correct
            redisService.setInitialClassScheduleSlots(schedule.getId(), currentAvailableSlots); // Re-initialize
        }

        int currentParticipants = schedule.getMaxParticipants() - currentAvailableSlots;
        System.out.println("currentParticipants " + currentParticipants);

        boolean isBookedByUser = false;
        boolean isOnWaitlistByUser = false;

        if (currentUser != null) {
            isBookedByUser = bookingRepository.existsByUserAndClassScheduleAndStatus(currentUser, schedule, BookingStatus.BOOKED);
            isOnWaitlistByUser = waitlistRepository.existsByUserAndClassSchedule(currentUser, schedule);
        }


        return ClassScheduleDTO.builder()
                .id(schedule.getId())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .requiredCredits(schedule.getRequiredCredits())
                .maxParticipants(schedule.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .availableSlots(currentAvailableSlots)
                .isFull(currentAvailableSlots <= 0)
                .classInfoId(schedule.getClassInfo().getId())
                .classInfoName(schedule.getClassInfo().getName())
                .classInfoDescription(schedule.getClassInfo().getDescription())
                .countryId(schedule.getClassInfo().getCountry().getId())
                .countryName(schedule.getClassInfo().getCountry().getName())
                .isBookedByUser(isBookedByUser)
                .isOnWaitlistByUser(isOnWaitlistByUser)
                .build();
    }
}