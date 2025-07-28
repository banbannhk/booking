package com.example.booking.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    /**
     * Mock method to send verification email.
     * @param email User's email address.
     * @return true if "sent" successfully.
     */
    public boolean sendVerifyEmail(String email) {
        logger.info("Mock send verification email to: {}", email);
        // Simulate success
        return true;
    }

}
