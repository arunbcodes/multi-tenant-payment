package com.example.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Base entity class with common fields.
 * Uses Lombok to generate getters, setters, equals, hashCode, and toString methods.
 */
@Data
@EqualsAndHashCode
public abstract class BaseEntity {
    
    protected Long id;
    
    @NotNull
    protected LocalDateTime createdAt;
    
    protected LocalDateTime updatedAt;
    
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }
}