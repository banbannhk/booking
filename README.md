# Online Class Booking System API

## 🚀 Project Overview

This project implements a robust backend API for a mobile class booking system. It handles user authentication, package purchases, class scheduling, booking management, waitlisting with credit handling, and concurrent booking control. The system is designed to be highly scalable and resilient, leveraging modern Java Spring Boot technologies and containerization with Docker.

## ✨ Features

* **User Management:**
  * User Registration & Login (JWT authenticated).
  * User Profile Management.
  * Email Verification (mocked for demo purposes).
  * Password Reset (mocked for demo purposes).
* **Package Management:**
  * Users can view available packages with varying credits, prices, and expiry dates.
  * Packages are country-specific (e.g., Singapore packages for Singapore classes).
  * Users can purchase packages (payment integration mocked).
* **Class Scheduling & Booking:**
  * View class schedules filtered by country.
  * Book classes using credits from a matching country package.
  * Automatic credit deduction upon successful booking.
  * Prevents overlapping class bookings for a single user.
* **Concurrency Control:**
  * Utilizes **Redis distributed locks (Redisson)** to prevent overbooking when multiple users try to book the same class concurrently.
* **Waitlist Management:**
  * If a class is full, users can join a waitlist.
  * **Credits are deducted immediately** when a user joins the waitlist.
  * If a booked user cancels, the first user on the waitlist is automatically promoted to a booked status.
* **Cancellation & Refunds:**
  * Users can cancel booked classes.
  * **Full credit refund** if canceled at least 4 hours before the class start time.
  * **No refund** if canceled within 4 hours of the class start time.
* **Scheduled Waitlist Refunds:**
  * A **Quartz scheduler job** automatically refunds credits to waitlisted users if the class ends and they were never promoted to a booking.
* **Class Check-in:**
  * Users can check into a booked class.
  * Check-in is allowed only within a specific window: **30 minutes before class starts until the class ends.**
* **Secure Authentication:**
  * Utilizes JSON Web Tokens (JWT) for secure user authentication and authorization.

## 🛠️ Technologies Used

* **Backend:** Java 17 (or your specific JDK version)
* **Framework:** Spring Boot 3.x
* **Database:** MySQL (or PostgreSQL)
  * ORM: Spring Data JPA / Hibernate
  * **Containerized via Docker for local development/deployment.**
* **Caching/Concurrency:** Redis
  * **Containerized via Docker for local development/deployment.**
  * Client: Redisson client for distributed locks
* **Scheduler:** Quartz Scheduler
* **Authentication:** Spring Security, JJWT (Java JWT)
* **Build Tool:** Maven (or Gradle)
* **API Documentation:** Swagger UI / OpenAPI 3 (if integrated)
* **Logging:** SLF4J with Logback (Spring Boot default)
* **Containerization:** Docker, Docker Compose

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed on your system:

* Java Development Kit (JDK) [PLACEHOLDER: e.g., 17 or 8]
* Maven [PLACEHOLDER: or Gradle]
* **Docker and Docker Compose** (essential for running the database and Redis services easily)

### 1. Generate Your JWT Secret Key

For security, you must generate a strong, unique JWT secret key. This key will be used by your application to sign and verify JWTs.

1.  Create a simple Java class (e.g., `KeyGenerator.java`) with a `main` method:
    ```java
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.io.Encoders;
    import io.jsonwebtoken.security.Keys;

    public class KeyGenerator {
        public static void main(String[] args) {
            java.security.Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // Generate a secure key for HS256
            String base64Key = Encoders.BASE64.encode(key.getEncoded());
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("YOUR SECURE JWT SECRET KEY (Base64 Encoded): " + base64Key);
            System.out.println("----------------------------------------------------------------------------------");
        }
    }
    ```
2.  Run this `main` method.
3.  **Copy the generated `base64Key` string.** You will need to paste this into your `docker-compose.yml` and/or `application.properties`.

### 2. Configure Application Properties

Update your `src/main/resources/application.properties` (or `application.yml`) with application-specific configurations. Note that for Docker Compose, database and Redis connection details will primarily come from environment variables defined in `docker-compose.yml`.

```properties
# --- JWT Secret Key ---
# For local running without Docker Compose, paste the generated key here:
# jwt.secret=[YOUR_GENERATED_SECURE_BASE64_JWT_SECRET_KEY]

# --- Quartz Scheduler ---
spring.quartz.job-store-type=memory # For production, consider 'jdbc' for persistence
# If jdbc:
# spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate # or MySQLDelegate
# spring.quartz.properties.org.quartz.jobStore.useProperties=false

# --- Mock Services ---
app.email.verification.mock.enabled=true
app.payment.mock.enabled=true

# --- Database Schema Management ---
# If using `db_init` scripts with Docker, you might set ddl-auto to 'none' or 'validate'
# to prevent Hibernate from trying to create/update schema on its own.
# spring.jpa.hibernate.ddl-auto=update # Default, good for development without explicit scripts
# spring.jpa.hibernate.ddl-auto=none # Recommended if using db_init scripts exclusively

# If running directly without Docker Compose for DB/Redis, uncomment and configure:
# spring.datasource.url=jdbc:mysql://localhost:3306/Booking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
# spring.datasource.username=[YOUR_DB_USERNAME]
# spring.datasource.password=[YOUR_DB_PASSWORD]
# spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# spring.data.redis.host=localhost
# spring.data.redis.port=6379
# spring.data.redis.password=[YOUR_REDIS_PASSWORD_IF_ANY]