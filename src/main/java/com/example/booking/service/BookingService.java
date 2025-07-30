package com.example.booking.service;

import com.example.booking.dto.BookingDTO;
import com.example.booking.dto.request.BookingRequest;
import com.example.booking.dto.request.CheckInRequest;
import com.example.booking.dto.WaitlistDTO;
import com.example.booking.entity.User;

import java.util.List;

public interface BookingService {
    BookingDTO bookClass(User user, BookingRequest request);
    BookingDTO cancelBooking(User user, Long bookingId);
    List<BookingDTO> getMyBookings(User user);
    List<WaitlistDTO> getMyWaitlists(User user);
    String checkIn(User user, CheckInRequest request);

    // Methods for scheduled tasks / internal logic
    void processWaitlistAfterCancellation(Long classScheduleId);
    void refundWaitlistCreditsAfterClassEnds();
}