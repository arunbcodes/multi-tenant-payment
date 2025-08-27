# Custom @TenantId Annotation System

## Overview

This document explains how our custom `@TenantId` annotation system works to automatically extract and validate tenant IDs from HTTP headers, eliminating repetitive boilerplate code across controllers.

## Architecture Components

### 1. Custom Annotation (`@TenantId`)
**Location:** `common-lib/src/main/java/com/example/common/annotation/TenantId.java`

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantId {
    boolean required() default true;
    String defaultValue() default "";
}
```

**Purpose:**
- Marks method parameters that should receive tenant ID values
- Provides configuration options (required, default value)
- Acts as a marker for Spring's argument resolution system

**Key Annotations:**
- `@Target(ElementType.PARAMETER)`: Can only be applied to method parameters
- `@Retention(RetentionPolicy.RUNTIME)`: Available at runtime for reflection

### 2. Argument Resolver (`TenantIdArgumentResolver`)
**Location:** `common-lib/src/main/java/com/example/common/resolver/TenantIdArgumentResolver.java`

```java
public class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TenantId.class) && 
               parameter.getParameterType().equals(String.class);
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter, ...) {
        // Extract from X-Tenant-ID header
        // Validate based on annotation settings
        // Return processed value
    }
}
```

**Purpose:**
- Implements Spring's `HandlerMethodArgumentResolver` interface
- Automatically called by Spring when it sees `@TenantId` annotation
- Extracts tenant ID from `X-Tenant-ID` header
- Performs validation and error handling

**Key Methods:**
- `supportsParameter()`: Tells Spring this resolver handles `@TenantId` annotated String parameters
- `resolveArgument()`: Does the actual work of extracting and validating the tenant ID

### 3. Web Configuration (`WebConfig`)
**Locations:** 
- `payment-service/src/main/java/com/example/payment/config/WebConfig.java`
- `processor-service/src/main/java/com/example/processor/config/WebConfig.java`

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new TenantIdArgumentResolver());
    }
}
```

**Purpose:**
- Registers our custom argument resolver with Spring MVC
- Required in each service that wants to use the `@TenantId` annotation
- Part of Spring's configuration system

## How They Work Together

### Step-by-Step Flow

1. **Application Startup:**
   ```
   Spring Boot starts up
   → Finds WebConfig classes
   → Calls addArgumentResolvers()
   → Registers TenantIdArgumentResolver
   → Spring MVC now knows about our custom resolver
   ```

2. **Request Processing:**
   ```
   HTTP Request arrives with X-Tenant-ID header
   → Spring MVC maps to controller method
   → Spring sees @TenantId annotation on parameter
   → Calls supportsParameter() on all resolvers
   → TenantIdArgumentResolver returns true
   → Spring calls resolveArgument()
   → Resolver extracts and validates tenant ID
   → Spring injects resolved value into method parameter
   → Controller method executes with clean tenant ID
   ```

### Visual Flow Diagram

```
HTTP Request
    ↓
Spring MVC DispatcherServlet
    ↓
Controller Method Detection
    ↓
Parameter Resolution Phase
    ↓
@TenantId annotation found?
    ↓ YES
TenantIdArgumentResolver.supportsParameter() → true
    ↓
TenantIdArgumentResolver.resolveArgument()
    ↓
Extract from X-Tenant-ID header
    ↓
Validate (required? empty? null?)
    ↓
Return validated tenant ID
    ↓
Spring injects into method parameter
    ↓
Controller method executes
```

## Usage Examples

### Before (Repetitive):
```java
@GetMapping("/{paymentId}")
public ResponseEntity<Payment> getPayment(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @PathVariable String paymentId) {
    // Manual validation needed
    if (tenantId == null || tenantId.trim().isEmpty()) {
        throw new IllegalArgumentException("Tenant ID required");
    }
    String cleanTenantId = tenantId.trim();
    // ... business logic
}
```

### After (Clean):
```java
@GetMapping("/{paymentId}")
public ResponseEntity<Payment> getPayment(
        @TenantId String tenantId,  // Automatically extracted and validated!
        @PathVariable String paymentId) {
    // tenantId is guaranteed to be valid here
    // ... business logic
}
```

### Advanced Usage:
```java
// Required tenant ID (default behavior)
public void method1(@TenantId String tenantId) { }

// Optional tenant ID
public void method2(@TenantId(required = false) String tenantId) { }

// Optional with default value
public void method3(@TenantId(required = false, defaultValue = "default-tenant") String tenantId) { }
```

## Benefits

1. **DRY Principle**: Eliminates repetitive `@RequestHeader("X-Tenant-ID")` annotations
2. **Centralized Validation**: All tenant ID validation logic in one place
3. **Consistent Error Messages**: Standardized error handling across all endpoints
4. **Type Safety**: Compile-time checking of parameter usage
5. **Maintainability**: Changes to tenant handling logic require updates in only one place
6. **Clean Controllers**: Controllers focus on business logic, not parameter extraction
7. **Reusability**: Works across all Spring controllers in the project

## Key Design Decisions

1. **Why Custom Annotation vs Base Controller?**
   - More flexible: can be used on any method parameter
   - Less inheritance coupling
   - Follows Spring's annotation-driven approach
   - Easier to test and mock

2. **Why String Parameter Type?**
   - Simple and lightweight
   - Easy to validate and process
   - Compatible with existing codebase
   - Can be extended to custom types if needed

3. **Why WebConfig in Each Service?**
   - Service isolation: each service configures its own resolvers
   - Flexibility: services can have different resolver configurations
   - Clear ownership: obvious which service uses which features

## Integration Points

- **Common Library**: Contains the annotation and resolver for reuse
- **Service Configuration**: Each service registers the resolver
- **Controller Usage**: Controllers use the annotation on method parameters
- **HTTP Headers**: Client must send `X-Tenant-ID` header

## Future Enhancements

1. **Custom Tenant Type**: Create a `Tenant` class instead of String
2. **Multiple Header Support**: Support multiple header names
3. **Caching**: Cache validated tenant IDs for performance
4. **Metrics**: Add metrics for tenant ID validation failures
5. **Security**: Integrate with security context for authorization

---

**Last Updated:** 2024-12-19  
**Version:** 1.0  
**Related Features:** Multi-tenant Support, Controller Refactoring
