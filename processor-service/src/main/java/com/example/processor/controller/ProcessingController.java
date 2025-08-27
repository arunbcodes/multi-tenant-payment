package com.example.processor.controller;

import com.example.common.annotation.TenantId;
import com.example.processor.model.ProcessingRequest;
import com.example.processor.service.ProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/processing")
@RequiredArgsConstructor
public class ProcessingController {
    
    private final ProcessingService processingService;
    
    @PostMapping("/payment/{paymentId}")
    public ResponseEntity<ProcessingRequest> createProcessingRequest(
            @TenantId String tenantId,
            @PathVariable String paymentId) {
        log.info("Creating processing request for payment: {} in tenant: {}", paymentId, tenantId);
        ProcessingRequest request = processingService.createProcessingRequest(tenantId, paymentId);
        log.info("Processing request created successfully: {} for payment: {} in tenant: {}", 
                   request.getRequestId(), paymentId, tenantId);
        return new ResponseEntity<>(request, HttpStatus.CREATED);
    }
    
    @GetMapping("/{requestId}")
    public ResponseEntity<ProcessingRequest> getProcessingRequest(
            @TenantId String tenantId,
            @PathVariable String requestId) {
        log.debug("Retrieving processing request: {} for tenant: {}", requestId, tenantId);
        return processingService.findByRequestId(tenantId, requestId)
                .map(request -> {
                    log.debug("Processing request found: {} for tenant: {}", requestId, tenantId);
                    return ResponseEntity.ok(request);
                })
                .orElseGet(() -> {
                    log.warn("Processing request not found: {} for tenant: {}", requestId, tenantId);
                    return ResponseEntity.notFound().build();
                });
    }
    
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<ProcessingRequest>> getProcessingRequestsByPayment(
            @TenantId String tenantId,
            @PathVariable String paymentId) {
        log.debug("Retrieving processing requests for payment: {} in tenant: {}", paymentId, tenantId);
        List<ProcessingRequest> requests = processingService.findByPaymentId(tenantId, paymentId);
        log.debug("Found {} processing requests for payment: {} in tenant: {}", 
                    requests.size(), paymentId, tenantId);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/tenant")
    public ResponseEntity<List<ProcessingRequest>> getAllProcessingRequestsForTenant(
            @TenantId String tenantId) {
        log.debug("Retrieving all processing requests for tenant: {}", tenantId);
        List<ProcessingRequest> requests = processingService.findByTenantId(tenantId);
        log.debug("Found {} processing requests for tenant: {}", requests.size(), tenantId);
        return ResponseEntity.ok(requests);
    }
    
    @PostMapping("/{requestId}/process")
    public ResponseEntity<String> processPayment(
            @TenantId String tenantId,
            @PathVariable String requestId) {
        try {
            log.info("Starting asynchronous processing for request: {} in tenant: {}", requestId, tenantId);
            // Process asynchronously
            CompletableFuture.runAsync(() -> processingService.processPayment(tenantId, requestId));
            log.info("Asynchronous processing initiated for request: {} in tenant: {}", requestId, tenantId);
            return ResponseEntity.ok("Processing started for request: " + requestId + " in tenant: " + tenantId);
        } catch (Exception e) {
            log.error("Failed to start processing for request: {} in tenant: {} - {}", 
                        requestId, tenantId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start processing: " + e.getMessage());
        }
    }
}