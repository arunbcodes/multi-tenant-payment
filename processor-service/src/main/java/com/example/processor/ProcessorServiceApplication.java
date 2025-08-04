package com.example.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ProcessorServiceApplication {
    
    public static void main(String[] args) {
        log.info("Starting Processor Service Application...");
        try {
            SpringApplication.run(ProcessorServiceApplication.class, args);
            log.info("Processor Service Application started successfully");
        } catch (Exception e) {
            log.error("Failed to start Processor Service Application: {}", e.getMessage());
            throw e;
        }
    }
}