package com.example.processor.repository;

import com.example.processor.model.ProcessingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessingRequestRepository extends JpaRepository<ProcessingRequest, Long> {
    
    // Tenant-aware query methods for multi-tenant isolation
    Optional<ProcessingRequest> findByTenantIdAndRequestId(String tenantId, String requestId);
    
    List<ProcessingRequest> findByTenantIdAndPaymentId(String tenantId, String paymentId);
    
    List<ProcessingRequest> findByTenantIdAndStatus(String tenantId, ProcessingRequest.ProcessingStatus status);
    
    List<ProcessingRequest> findByTenantId(String tenantId);
}