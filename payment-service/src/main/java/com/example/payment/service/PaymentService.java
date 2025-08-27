package com.example.payment.service;

import com.example.common.util.DateUtils;
import com.example.payment.model.Payment;
import com.example.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    public Payment createPayment(Payment payment) {
        // Demonstrate Lombok's generated builder pattern and methods
        Payment enrichedPayment = Payment.builder()
                .tenantId(payment.getTenantId())
                .paymentId(UUID.randomUUID().toString())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .customerId(payment.getCustomerId())
                .status(Payment.PaymentStatus.PENDING)
                .build();
        
        enrichedPayment.setCreatedAt(LocalDateTime.now());
        
        log.info("Creating payment {} at {}", 
                   enrichedPayment.getPaymentId(), 
                   DateUtils.format(enrichedPayment.getCreatedAt()));
        
        // Demonstrate Lombok's generated toString method
        log.debug("Payment details: {}", enrichedPayment.toString());
        
        return paymentRepository.save(enrichedPayment);
    }
    
    public Optional<Payment> findByPaymentId(String tenantId, String paymentId) {
        return paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId);
    }
    
    public List<Payment> findByCustomerId(String tenantId, String customerId) {
        return paymentRepository.findByTenantIdAndCustomerId(tenantId, customerId);
    }
    
    public List<Payment> findByTenantId(String tenantId) {
        return paymentRepository.findByTenantId(tenantId);
    }
    
    public Payment updatePaymentStatus(String tenantId, String paymentId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId + " for tenant: " + tenantId));
        
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        
        log.info("Updated payment {} status to {} for tenant {} at {}", 
                   paymentId, status, tenantId, DateUtils.formatNow());
        
        return paymentRepository.save(payment);
    }
}