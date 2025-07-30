package com.example.booking.controller;

import com.example.booking.dto.BookingDTO;
import com.example.booking.dto.request.BookingRequest;
import com.example.booking.dto.request.CheckInRequest;
import com.example.booking.dto.WaitlistDTO;
import com.example.booking.dto.response.ErrorResponse;
import com.example.booking.entity.User;
import com.example.booking.service.AuthService;
import com.example.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("isAuthenticated()")
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    public BookingController(BookingService bookingService, AuthService authService) {
        this.bookingService = bookingService;
        this.authService = authService;
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookClass(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody BookingRequest request) {
        User user = authService.getUserProfile(userDetails.getUsername());
        try {
            BookingDTO booking = bookingService.bookClass(user, request);
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } catch (com.example.booking.exception.BadRequestException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancelBooking(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable Long bookingId) {
        User user = authService.getUserProfile(userDetails.getUsername());
        try {
            BookingDTO canceledBooking = bookingService.cancelBooking(user, bookingId);
            return ResponseEntity.ok(canceledBooking);
        } catch (com.example.booking.exception.BadRequestException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingDTO>> getMyBookings(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserProfile(userDetails.getUsername());
        List<BookingDTO> bookings = bookingService.getMyBookings(user);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-waitlists")
    public ResponseEntity<List<WaitlistDTO>> getMyWaitlists(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserProfile(userDetails.getUsername());
        List<WaitlistDTO> waitlists = bookingService.getMyWaitlists(user);
        return ResponseEntity.ok(waitlists);
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody CheckInRequest request) {
        User user = authService.getUserProfile(userDetails.getUsername());
        try {
            String result = bookingService.checkIn(user, request);
            return ResponseEntity.ok(result);
        } catch (com.example.booking.exception.BadRequestException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (com.example.booking.exception.ResourceNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}