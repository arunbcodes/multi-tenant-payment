package com.example.processor.model;

import com.example.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processing_requests", indexes = {
    @Index(name = "idx_processing_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_processing_tenant_payment", columnList = "tenantId, paymentId"),
    @Index(name = "idx_processing_tenant_status", columnList = "tenantId, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProcessingRequest extends BaseEntity {
    
    @NotBlank
    @Column(nullable = false, length = 100)
    private String tenantId;
    
    @NotBlank
    @Column(nullable = false)
    private String requestId;
    
    @NotBlank
    @Column(nullable = false)
    private String paymentId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.PENDING;
    
    @Column(length = 1000)
    private String errorMessage;
    
    public enum ProcessingStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}