 package com.example.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

 @SpringBootApplication
@ComponentScan(basePackages = {"com.example.booking", "com.kafka"})
public class BookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingApplication.class, args);
	}

}