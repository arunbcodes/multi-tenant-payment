package com.example.payment.model;

import com.example.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_payment_tenant_customer", columnList = "tenantId, customerId"),
    @Index(name = "idx_payment_tenant_status", columnList = "tenantId, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Payment extends BaseEntity {
    
    @NotBlank
    @Column(nullable = false, length = 100)
    private String tenantId;
    
    @NotBlank
    @Column(nullable = false)
    private String paymentId;
    
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @NotBlank
    @Column(nullable = false)
    private String currency;
    
    @NotBlank
    @Column(nullable = false)
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
    }
}