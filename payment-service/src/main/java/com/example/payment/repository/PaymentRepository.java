package com.example.payment.repository;

import com.example.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Tenant-aware query methods for multi-tenant isolation
    Optional<Payment> findByTenantIdAndPaymentId(String tenantId, String paymentId);
    
    List<Payment> findByTenantIdAndCustomerId(String tenantId, String customerId);
    
    List<Payment> findByTenantIdAndStatus(String tenantId, Payment.PaymentStatus status);
    
    List<Payment> findByTenantId(String tenantId);
}