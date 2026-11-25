# Candidate Management System — Backend Architecture

## Technology

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Jakarta Bean Validation
- Springdoc OpenAPI

## Layered structure

```text
controller
  REST request/response handling

service
  Candidate use-case contracts

service.impl
  CRUD and search business logic

repository
  JPA repositories and dynamic Specifications

entity
  JPA persistence model

dto
  API request and response types

mapper
  Entity/DTO conversion

exception
  Custom exceptions and centralized API error handling
```

The application remains a monolith. It is modular by responsibility but has one deployable process and one PostgreSQL database.

## Component responsibilities

### Controller

Owns HTTP mapping, `@Valid` request validation, status codes, and URI construction.

### Service

Owns business operations, duplicate-email checks, not-found behavior, range validation, pagination, and sorting.

### Repository

Owns persistence operations and database-side dynamic specifications.

### Entity

Owns JPA mappings and persistence lifecycle timestamps.

### DTO

Keeps persistence objects out of the API contract.

### Mapper

Normalizes and converts between persistence and API representations.

### Exception handler

Returns consistent HTTP errors without exposing SQL, stack traces, or internal exception text.

## Design decisions

- UUID identifiers avoid exposing sequential database IDs.
- DTOs prevent entity serialization and persistence leakage.
- JPA Specifications avoid a repository method for every filter combination.
- Flyway owns the database schema.
- Hibernate uses `ddl-auto=validate`.
- Entity and transaction state remains server-side.
- The application has no authentication or authorization unless separately approved.
