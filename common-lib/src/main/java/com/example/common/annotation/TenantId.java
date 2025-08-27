package com.example.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to automatically inject tenant ID from the X-Tenant-ID header.
 * This eliminates the need to repeat @RequestHeader("X-Tenant-ID") in every controller method.
 * 
 * Usage:
 * <pre>
 * public ResponseEntity<Payment> getPayment(@TenantId String tenantId, @PathVariable String paymentId) {
 *     // tenantId is automatically extracted from X-Tenant-ID header
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantId {
    /**
     * Whether the tenant ID is required. If true and no tenant ID is provided,
     * an exception will be thrown.
     */
    boolean required() default true;
    
    /**
     * Default value to use if no tenant ID is provided and required=false.
     */
    String defaultValue() default "";
}
