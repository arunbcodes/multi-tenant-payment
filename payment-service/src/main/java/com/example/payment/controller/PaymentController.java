package com.example.payment.controller;

import com.example.common.annotation.TenantId;
import com.example.payment.model.Payment;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @TenantId String tenantId,
            @Valid @RequestBody Payment payment) {
        log.info("Creating payment for tenant: {} customer: {}", tenantId, payment.getCustomerId());
        
        // Ensure payment has the correct tenant ID from header
        payment.setTenantId(tenantId);
        Payment createdPayment = paymentService.createPayment(payment);
        log.info("Payment created successfully: {} for tenant: {}", createdPayment.getPaymentId(), createdPayment.getTenantId());
        return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(
            @TenantId String tenantId,
            @PathVariable String paymentId) {
        log.debug("Retrieving payment: {} for tenant: {}", paymentId, tenantId);
        return paymentService.findByPaymentId(tenantId, paymentId)
                .map(payment -> {
                    log.debug("Payment found: {} for tenant: {}", paymentId, tenantId);
                    return ResponseEntity.ok(payment);
                })
                .orElseGet(() -> {
                    log.warn("Payment not found: {} for tenant: {}", paymentId, tenantId);
                    return ResponseEntity.notFound().build();
                });
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Payment>> getPaymentsByCustomer(
            @TenantId String tenantId,
            @PathVariable String customerId) {
        log.debug("Retrieving payments for customer: {} in tenant: {}", customerId, tenantId);
        List<Payment> payments = paymentService.findByCustomerId(tenantId, customerId);
        log.debug("Found {} payments for customer: {} in tenant: {}", payments.size(), customerId, tenantId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/tenant")
    public ResponseEntity<List<Payment>> getAllPaymentsForTenant(@TenantId String tenantId) {
        log.debug("Retrieving all payments for tenant: {}", tenantId);
        List<Payment> payments = paymentService.findByTenantId(tenantId);
        log.debug("Found {} payments for tenant: {}", payments.size(), tenantId);
        return ResponseEntity.ok(payments);
    }
    
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @TenantId String tenantId,
            @PathVariable String paymentId,
            @RequestParam Payment.PaymentStatus status) {
        try {
            log.info("Updating payment status: {} to {} for tenant: {}", paymentId, status, tenantId);
            Payment updatedPayment = paymentService.updatePaymentStatus(tenantId, paymentId, status);
            log.info("Payment status updated successfully: {} to {} for tenant: {}", paymentId, status, tenantId);
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            log.error("Failed to update payment status: {} for tenant: {} - {}", paymentId, tenantId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}