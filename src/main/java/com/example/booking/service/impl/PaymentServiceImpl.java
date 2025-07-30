package com.example.booking.service.impl;

import com.example.booking.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public boolean chargePayment(String userId, BigDecimal amount, String currency) {
        System.out.println("PaymentService: Payment of " + amount + " " + currency + " charged successfully for user " + userId);
        return true;
    }
}