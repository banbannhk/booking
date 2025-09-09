package com.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class BookingEvent {
    @JsonProperty("eventType")
    private BookingEventType eventType;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("userEmail")
    private String userEmail;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("bookingId")
    private Long bookingId;

    @JsonProperty("classId")
    private Long classId;

    @JsonProperty("className")
    private String className;

    @JsonProperty("classDateTime")
    private LocalDateTime classDateTime;

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("creditsUsed")
    private Integer creditsUsed;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    // Constructors
    public BookingEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public BookingEvent(BookingEventType eventType, Long userId, String userEmail, String userName) {
        this();
        this.eventType = eventType;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
    }

    // Getters and Setters
    public BookingEventType getEventType() { return eventType; }
    public void setEventType(BookingEventType eventType) { this.eventType = eventType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public LocalDateTime getClassDateTime() { return classDateTime; }
    public void setClassDateTime(LocalDateTime classDateTime) { this.classDateTime = classDateTime; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public Integer getCreditsUsed() { return creditsUsed; }
    public void setCreditsUsed(Integer creditsUsed) { this.creditsUsed = creditsUsed; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "BookingEvent{" +
                "eventType=" + eventType +
                ", userId=" + userId +
                ", userEmail='" + userEmail + '\'' +
                ", bookingId=" + bookingId +
                ", className='" + className + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}