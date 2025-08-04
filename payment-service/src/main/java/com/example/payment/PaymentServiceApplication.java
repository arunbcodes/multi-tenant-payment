package com.example.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PaymentServiceApplication {
    
    public static void main(String[] args) {
        log.info("Starting Payment Service Application...");
        try {
            SpringApplication.run(PaymentServiceApplication.class, args);
            log.info("Payment Service Application started successfully");
        } catch (Exception e) {
            log.error("Failed to start Payment Service Application: {}", e.getMessage());
            throw e;
        }
    }
}