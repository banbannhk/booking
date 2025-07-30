package com.example.booking.entity;

public enum WaitlistStatus {
    WAITLISTED,            // User is actively on the waitlist
    CONVERTED_TO_BOOKED,   // User was moved from waitlist to an actual booking
    REFUNDED               // User's credits were refunded because they were still on waitlist when class ended
}
