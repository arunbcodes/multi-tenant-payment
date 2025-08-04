# Multi-Tenant Payment System

This project demonstrates a multi-module Gradle setup with three modules:

## Modules

### 1. common-lib
A shared library containing common utilities and base classes used by the other modules.

**Features:**
- Base entity class with common fields (id, createdAt, updatedAt)
- Date utility functions
- Common validation annotations

### 2. payment-service
A Spring Boot microservice for handling payment operations.

**Features:**
- RESTful API for payment management
- Payment entity with JPA persistence
- H2 in-memory database
- Actuator endpoints for monitoring
- Runs on port 8081

**Endpoints:**
- `POST /api/payments` - Create a payment
- `GET /api/payments/{paymentId}` - Get payment by ID
- `GET /api/payments/customer/{customerId}` - Get payments by customer
- `PUT /api/payments/{paymentId}/status` - Update payment status

### 3. processor-service
A Spring Boot microservice for processing payment requests.

**Features:**
- Processing request management
- Asynchronous payment processing
- H2 in-memory database
- Actuator endpoints for monitoring
- Runs on port 8082

**Endpoints:**
- `POST /api/processing/payment/{paymentId}` - Create processing request
- `GET /api/processing/{requestId}` - Get processing request by ID
- `GET /api/processing/payment/{paymentId}` - Get processing requests by payment
- `POST /api/processing/{requestId}/process` - Start processing

## Building and Running

### Build all modules
```bash
./gradlew build
```

### Run individual services
```bash
# Payment Service (port 8081)
./gradlew :payment-service:bootRun

# Processor Service (port 8082)
./gradlew :processor-service:bootRun
```

### Test all modules
```bash
./gradlew test
```

## Project Structure
```
multi-tenant-payment/
├── build.gradle                 # Root build configuration
├── settings.gradle              # Module definitions
├── common-lib/                  # Shared library module
│   ├── build.gradle
│   └── src/main/java/com/example/common/
├── payment-service/             # Payment microservice
│   ├── build.gradle
│   ├── src/main/java/com/example/payment/
│   └── src/main/resources/
└── processor-service/           # Processing microservice
    ├── build.gradle
    ├── src/main/java/com/example/processor/
    └── src/main/resources/
```

## Dependencies

- **Java 17**
- **Spring Boot 3.2.0**
- **H2 Database** (for development)
- **JUnit 5** (for testing)
- **Apache Commons Lang3**
- **Jackson** (JSON processing)