package com.kafka.service;

import com.kafka.event.BookingEvent;
import com.kafka.event.BookingEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @KafkaListener(topics = "booking-events", groupId = "booking-service-group")
    public void handleBookingEvent(BookingEvent event) {
        logger.info("📧 Processing booking event: {} for user: {}",
                event.getEventType(), event.getUserEmail());

        try {
            switch (event.getEventType()) {
                case CLASS_BOOKED:
                    sendBookingConfirmationEmail(event);
                    break;
                case CLASS_CANCELLED:
                    sendCancellationEmail(event);
                    break;
                case WAITLIST_JOINED:
                    sendWaitlistConfirmationEmail(event);
                    break;
                case WAITLIST_PROMOTED:
                    sendWaitlistPromotionEmail(event);
                    break;
                case WAITLIST_REFUNDED:
                    sendWaitlistRefundEmail(event);
                    break;
                case PACKAGE_PURCHASED:
                    sendPackagePurchaseConfirmationEmail(event);
                    break;
                case CLASS_CHECKED_IN:
                    sendCheckInConfirmationEmail(event);
                    break;
                case BOOKING_REMINDER:
                    sendBookingReminderEmail(event);
                    break;
                default:
                    logger.warn("❓ Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("❌ Failed to process booking event: {}", e.getMessage());
            // You could implement dead letter queue or retry logic here
        }
    }

    private void sendBookingConfirmationEmail(BookingEvent event) {
        logger.info("✅ Sending booking confirmation email to: {}", event.getUserEmail());
        logger.info("   Class: {} at {}", event.getClassName(), event.getClassDateTime());
        logger.info("   Credits used: {}", event.getCreditsUsed());

        // TODO: Integrate with your actual email service
        // Example integration points:
        // - AWS SES
        // - SendGrid
        // - JavaMail API
        // - Your existing email mock service

        simulateEmailSending(event.getUserEmail(), "Booking Confirmation",
                "Your booking for " + event.getClassName() + " is confirmed!");
    }

    private void sendCancellationEmail(BookingEvent event) {
        logger.info("❌ Sending cancellation email to: {}", event.getUserEmail());
        logger.info("   Class: {}", event.getClassName());

        simulateEmailSending(event.getUserEmail(), "Booking Cancelled",
                "Your booking for " + event.getClassName() + " has been cancelled.");
    }

    private void sendWaitlistConfirmationEmail(BookingEvent event) {
        logger.info("⏳ Sending waitlist confirmation email to: {}", event.getUserEmail());
        logger.info("   Class: {}", event.getClassName());

        simulateEmailSending(event.getUserEmail(), "Waitlist Confirmation",
                "You've been added to the waitlist for " + event.getClassName());
    }

    private void sendWaitlistPromotionEmail(BookingEvent event) {
        logger.info("🎉 Sending waitlist promotion email to: {}", event.getUserEmail());
        logger.info("   Class: {}", event.getClassName());

        simulateEmailSending(event.getUserEmail(), "Waitlist Promotion",
                "Great news! You've been promoted from waitlist to booked for " + event.getClassName());
    }

    private void sendWaitlistRefundEmail(BookingEvent event) {
        logger.info("💰 Sending waitlist refund email to: {}", event.getUserEmail());

        simulateEmailSending(event.getUserEmail(), "Waitlist Refund",
                "Your credits have been refunded for the waitlisted class.");
    }

    private void sendPackagePurchaseConfirmationEmail(BookingEvent event) {
        logger.info("💳 Sending package purchase confirmation email to: {}", event.getUserEmail());

        simulateEmailSending(event.getUserEmail(), "Package Purchase Confirmation",
                "Thank you for purchasing the " + event.getPackageName() + " package!");
    }

    private void sendCheckInConfirmationEmail(BookingEvent event) {
        logger.info("✅ Sending check-in confirmation email to: {}", event.getUserEmail());

        simulateEmailSending(event.getUserEmail(), "Check-in Confirmation",
                "You've successfully checked in to " + event.getClassName());
    }

    private void sendBookingReminderEmail(BookingEvent event) {
        logger.info("⏰ Sending booking reminder email to: {}", event.getUserEmail());

        simulateEmailSending(event.getUserEmail(), "Class Reminder",
                "Reminder: Your class " + event.getClassName() + " starts soon!");
    }

    private void simulateEmailSending(String email, String subject, String content) {
        // Since you already have email mocking in your system,
        // you can integrate with your existing email service here
        logger.info("📧 EMAIL SENT:");
        logger.info("   To: {}", email);
        logger.info("   Subject: {}", subject);
        logger.info("   Content: {}", content);
    }
}