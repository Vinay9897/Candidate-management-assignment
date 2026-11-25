# Candidate Management System — Testing Strategy

## Test layers

### Service unit tests

Use JUnit 5 and Mockito.

Coverage:

- Candidate creation
- Duplicate-email rejection
- Candidate retrieval
- Candidate not found
- Candidate update
- Email conflict during update
- Candidate deletion
- Delete of unknown candidate
- Page mapping and sorting delegation

Arrange, Act, Assert is used for each test.

### Controller tests

Use `@WebMvcTest`, MockMvc, and a mocked service.

Coverage:

- CRUD response statuses
- Request validation
- Search query binding
- Empty search pages
- Not-found responses
- Duplicate-email responses
- Safe database and server error responses
- Malformed IDs and query parameters

### Repository integration tests

Use Spring JPA tests against PostgreSQL.

Coverage:

- Entity persistence
- Case-insensitive email lookup
- Dynamic combined Specifications
- Partial and case-insensitive matching
- Experience ranges
- Pagination
- Sorting
- Empty result sets
- Database migration application

Testcontainers is optional. Docker is not required if the environment uses a dedicated local or CI PostgreSQL database.

## Repeatability

- Clean or isolate test data.
- Use UUID or unique generated values.
- Roll back full-context integration tests.
- Do not mock JPA, PostgreSQL, or Spring transactions in repository integration tests.
- Do not share mutable state between tests.
