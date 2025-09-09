package com.kafka.service;

import com.kafka.event.BookingEvent;
import com.kafka.event.BookingEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventPublisher.class);
    private static final String BOOKING_TOPIC = "booking-events";

    @Autowired
    private KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public void publishBookingEvent(BookingEvent event) {
        try {
            String key = event.getUserId() + "-" + event.getEventType();
            kafkaTemplate.send(BOOKING_TOPIC, key, event);
            logger.info("📨 Published booking event: {}", event.getEventType());
        } catch (Exception e) {
            logger.error("❌ Failed to publish booking event: {}", e.getMessage());
        }
    }

    // Convenience methods for different event types
    public void publishClassBooked(Long userId, String userEmail, String userName,
                                   Long bookingId, Long classId, String className,
                                   java.time.LocalDateTime classDateTime, Integer creditsUsed) {
        BookingEvent event = new BookingEvent(BookingEventType.CLASS_BOOKED, userId, userEmail, userName);
        event.setBookingId(bookingId);
        event.setClassId(classId);
        event.setClassName(className);
        event.setClassDateTime(classDateTime);
        event.setCreditsUsed(creditsUsed);
        publishBookingEvent(event);
    }

    public void publishClassCancelled(Long userId, String userEmail, String userName,
                                      Long bookingId, Long classId, String className) {
        BookingEvent event = new BookingEvent(BookingEventType.CLASS_CANCELLED, userId, userEmail, userName);
        event.setBookingId(bookingId);
        event.setClassId(classId);
        event.setClassName(className);
        publishBookingEvent(event);
    }

    public void publishWaitlistJoined(Long userId, String userEmail, String userName,
                                      Long classId, String className, Integer creditsUsed) {
        BookingEvent event = new BookingEvent(BookingEventType.WAITLIST_JOINED, userId, userEmail, userName);
        event.setClassId(classId);
        event.setClassName(className);
        event.setCreditsUsed(creditsUsed);
        publishBookingEvent(event);
    }

    public void publishWaitlistPromoted(Long userId, String userEmail, String userName,
                                        Long bookingId, Long classId, String className) {
        BookingEvent event = new BookingEvent(BookingEventType.WAITLIST_PROMOTED, userId, userEmail, userName);
        event.setBookingId(bookingId);
        event.setClassId(classId);
        event.setClassName(className);
        publishBookingEvent(event);
    }
}
