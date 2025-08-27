# Feature: Tenant Management System

## Overview

The tenant management system provides multi-tenant support through HTTP header-based tenant isolation. Each request must include a `X-Tenant-ID` header to identify the tenant context.

## Architecture

### Custom @TenantId Annotation
**Location:** `common-lib/src/main/java/com/example/common/annotation/TenantId.java`

Provides declarative tenant ID injection into controller method parameters.

```java
@GetMapping("/{paymentId}")
public ResponseEntity<Payment> getPayment(
        @TenantId String tenantId,  // Automatically extracted and validated
        @PathVariable String paymentId) {
    // tenantId is ready to use
}
```

### Argument Resolver
**Location:** `common-lib/src/main/java/com/example/common/resolver/TenantIdArgumentResolver.java`

Implements Spring's `HandlerMethodArgumentResolver` to automatically extract tenant IDs from HTTP headers.

**Key Features:**
- Automatic header extraction from `X-Tenant-ID`
- Validation (null/empty checks)
- Support for optional tenant IDs
- Consistent error messages

### Configuration
**Locations:** 
- `payment-service/src/main/java/com/example/payment/config/WebConfig.java`
- `processor-service/src/main/java/com/example/processor/config/WebConfig.java`

Registers the custom argument resolver with Spring MVC.

## Data Isolation Strategy

### Entity Level
All tenant-aware entities include a `tenantId` field:

```java
@Entity
public class Payment extends BaseEntity {
    private String tenantId;
    private String paymentId;
    // ... other fields
}
```

### Repository Level
Repository queries include tenant ID filtering:

```java
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTenantIdAndPaymentId(String tenantId, String paymentId);
    List<Payment> findByTenantIdAndCustomerId(String tenantId, String customerId);
    List<Payment> findByTenantId(String tenantId);
}
```

### Service Level
All service methods require tenant ID as first parameter:

```java
@Service
public class PaymentService {
    public Payment createPayment(Payment payment) {
        // payment.tenantId set by controller
    }
    
    public Optional<Payment> findByPaymentId(String tenantId, String paymentId) {
        return paymentRepository.findByTenantIdAndPaymentId(tenantId, paymentId);
    }
}
```

## Request Flow

1. **Client Request**
   ```http
   GET /api/payments/pay-123
   X-Tenant-ID: tenant-abc
   ```

2. **Spring Processing**
   - Request hits controller method
   - Spring sees `@TenantId` annotation
   - `TenantIdArgumentResolver` extracts `tenant-abc` from header
   - Validates non-null, non-empty
   - Injects clean value into method parameter

3. **Business Logic**
   ```java
   public ResponseEntity<Payment> getPayment(@TenantId String tenantId, @PathVariable String paymentId) {
       // tenantId = "tenant-abc" (validated)
       // paymentId = "pay-123"
       return paymentService.findByPaymentId(tenantId, paymentId);
   }
   ```

4. **Data Access**
   ```sql
   SELECT * FROM payment WHERE tenant_id = 'tenant-abc' AND payment_id = 'pay-123';
   ```

## Security Considerations

### Current Implementation
- **Header-based tenant identification** (X-Tenant-ID)
- **No authentication/authorization** implemented yet
- **Trust-based model** - client specifies tenant

### Security Gaps (To Be Addressed)
- ❌ No validation that user can access specified tenant
- ❌ No authentication of requests
- ❌ No audit logging of tenant access
- ❌ No rate limiting per tenant

### Future Security Enhancements
1. **JWT-based authentication** with tenant claims
2. **Role-based access control** (RBAC)
3. **Tenant membership validation**
4. **API key management per tenant**
5. **Audit logging** for compliance

## Usage Examples

### Required Tenant (Default)
```java
@GetMapping("/payments")
public List<Payment> getPayments(@TenantId String tenantId) {
    // tenantId is required - will throw exception if missing
}
```

### Optional Tenant
```java
@GetMapping("/health")
public String healthCheck(@TenantId(required = false) String tenantId) {
    // tenantId may be null
}
```

### Default Tenant
```java
@GetMapping("/public-info")
public String getInfo(@TenantId(required = false, defaultValue = "public") String tenantId) {
    // tenantId will be "public" if header is missing
}
```

## Error Handling

### Missing Header
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Missing required header: X-Tenant-ID. Please include the tenant ID in the request header."
}
```

### Empty Header
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Tenant ID cannot be empty. Please provide a valid tenant ID in the X-Tenant-ID header."
}
```

## Configuration Options

### Annotation Parameters
```java
public @interface TenantId {
    boolean required() default true;    // Is tenant ID mandatory?
    String defaultValue() default "";   // Default value if not required
}
```

### Resolver Configuration
```java
public class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String TENANT_HEADER_NAME = "X-Tenant-ID";  // Configurable
    
    // Validation logic
    // Error message formatting
    // Default value handling
}
```

## Integration Points

### Controllers
- **PaymentController:** All endpoints require tenant context
- **ProcessingController:** All endpoints require tenant context

### Services
- **PaymentService:** All methods include tenant parameter
- **ProcessingService:** All methods include tenant parameter

### Repositories
- **PaymentRepository:** All queries filtered by tenant
- **ProcessingRequestRepository:** All queries filtered by tenant

## Testing Strategy

### Unit Testing
```java
@Test
void testGetPayment() {
    PaymentController controller = new PaymentController(paymentService);
    
    // Test with valid tenant ID
    ResponseEntity<Payment> response = controller.getPayment("tenant-123", "pay-456");
    
    verify(paymentService).findByPaymentId("tenant-123", "pay-456");
}
```

### Integration Testing
```java
@SpringBootTest
@AutoConfigureTestDatabase
class TenantIntegrationTest {
    
    @Test
    void testTenantIsolation() {
        // Create payments for different tenants
        // Verify they are isolated
    }
}
```

## Performance Considerations

### Current Impact
- **Minimal overhead:** Simple string parameter injection
- **No caching:** Each request processes header fresh
- **Database impact:** Additional WHERE clause in all queries

### Optimization Opportunities
1. **Tenant ID caching** for request scope
2. **Connection pooling** per tenant
3. **Query optimization** with tenant-specific indexes
4. **Tenant-specific database schemas**

## Monitoring & Observability

### Metrics to Track
- Requests per tenant
- Tenant ID validation failures
- Response times by tenant
- Error rates by tenant

### Logging Strategy
```java
log.info("Processing payment {} for tenant {}", paymentId, tenantId);
log.warn("Payment not found: {} for tenant {}", paymentId, tenantId);
log.error("Failed to process payment {} for tenant {}: {}", paymentId, tenantId, error);
```

## Future Enhancements

### Short Term
1. **Tenant configuration management**
2. **Tenant-specific feature flags**
3. **Basic tenant validation**

### Medium Term
1. **Authentication integration**
2. **Role-based access control**
3. **Tenant onboarding workflow**

### Long Term
1. **Multi-database support**
2. **Tenant-specific schemas**
3. **Dynamic tenant scaling**

---

**Implemented:** 2024-12-19  
**Status:** Production Ready (with security limitations)  
**Dependencies:** Spring MVC, Lombok  
**Testing:** Unit tests implemented
