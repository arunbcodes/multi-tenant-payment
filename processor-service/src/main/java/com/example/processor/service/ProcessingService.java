package com.example.processor.service;

import com.example.common.util.DateUtils;
import com.example.processor.model.ProcessingRequest;
import com.example.processor.repository.ProcessingRequestRepository;
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
public class ProcessingService {
    
    private final ProcessingRequestRepository repository;
    
    public ProcessingRequest createProcessingRequest(String tenantId, String paymentId) {
        ProcessingRequest request = ProcessingRequest.builder()
                .tenantId(tenantId)
                .requestId(UUID.randomUUID().toString())
                .paymentId(paymentId)
                .build();
        
        request.setCreatedAt(LocalDateTime.now());
        
        log.info("Creating processing request {} for payment {} in tenant {} at {}", 
                   request.getRequestId(), paymentId, tenantId, DateUtils.formatNow());
        
        return repository.save(request);
    }
    
    public Optional<ProcessingRequest> findByRequestId(String tenantId, String requestId) {
        return repository.findByTenantIdAndRequestId(tenantId, requestId);
    }
    
    public List<ProcessingRequest> findByPaymentId(String tenantId, String paymentId) {
        return repository.findByTenantIdAndPaymentId(tenantId, paymentId);
    }
    
    public List<ProcessingRequest> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId);
    }
    
    public ProcessingRequest processPayment(String tenantId, String requestId) {
        ProcessingRequest request = repository.findByTenantIdAndRequestId(tenantId, requestId)
                .orElseThrow(() -> new RuntimeException("Processing request not found: " + requestId + " for tenant: " + tenantId));
        
        request.setStatus(ProcessingRequest.ProcessingStatus.IN_PROGRESS);
        request.setUpdatedAt(LocalDateTime.now());
        repository.save(request);
        
        try {
            // Simulate processing logic
            Thread.sleep(1000); // Simulate processing time
            
            // Mark as completed
            request.setStatus(ProcessingRequest.ProcessingStatus.COMPLETED);
            request.setUpdatedAt(LocalDateTime.now());
            
            log.info("Completed processing request {} for payment {} in tenant {} at {}", 
                       requestId, request.getPaymentId(), tenantId, DateUtils.formatNow());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            request.setStatus(ProcessingRequest.ProcessingStatus.FAILED);
            request.setErrorMessage("Processing interrupted: " + e.getMessage());
            request.setUpdatedAt(LocalDateTime.now());
            
            log.error("Failed processing request {} for payment {} in tenant {}: {}", 
                        requestId, request.getPaymentId(), tenantId, e.getMessage());
        } catch (Exception e) {
            request.setStatus(ProcessingRequest.ProcessingStatus.FAILED);
            request.setErrorMessage("Processing failed: " + e.getMessage());
            request.setUpdatedAt(LocalDateTime.now());
            
            log.error("Failed processing request {} for payment {} in tenant {}: {}", 
                        requestId, request.getPaymentId(), tenantId, e.getMessage());
        }
        
        return repository.save(request);
    }
}