package com.example.common.resolver;

import com.example.common.annotation.TenantId;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Custom argument resolver that automatically extracts tenant ID from the X-Tenant-ID header
 * when a method parameter is annotated with @TenantId.
 * 
 * This resolver eliminates the need to use @RequestHeader("X-Tenant-ID") in every controller method.
 */
public class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {
    
    private static final String TENANT_HEADER_NAME = "X-Tenant-ID";
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // This resolver supports parameters annotated with @TenantId
        return parameter.hasParameterAnnotation(TenantId.class) && 
               parameter.getParameterType().equals(String.class);
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter, 
                                ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, 
                                WebDataBinderFactory binderFactory) throws Exception {
        
        TenantId tenantIdAnnotation = parameter.getParameterAnnotation(TenantId.class);
        String tenantId = webRequest.getHeader(TENANT_HEADER_NAME);
        
        if (tenantId == null || tenantId.trim().isEmpty()) {
            if (tenantIdAnnotation.required()) {
                throw new IllegalArgumentException(
                    "Missing required header: " + TENANT_HEADER_NAME + 
                    ". Please include the tenant ID in the request header."
                );
            } else {
                // Use default value if not required
                String defaultValue = tenantIdAnnotation.defaultValue();
                return defaultValue.isEmpty() ? null : defaultValue;
            }
        }
        
        // Validate and return the tenant ID
        String trimmedTenantId = tenantId.trim();
        if (trimmedTenantId.isEmpty() && tenantIdAnnotation.required()) {
            throw new IllegalArgumentException(
                "Tenant ID cannot be empty. Please provide a valid tenant ID in the " + 
                TENANT_HEADER_NAME + " header."
            );
        }
        
        return trimmedTenantId;
    }
}
