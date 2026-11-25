# Candidate Management System — Scalability Technical Design

## Current architecture

The application is a Spring Boot monolith with one PostgreSQL database.

```text
Controller → Service → JPA Repository/Specification → PostgreSQL
```

This avoids unnecessary microservice infrastructure while the system is small or medium-sized.

## Database design

The candidate table uses a UUID primary key and normal scalar columns. V1 creates baseline schema and V2 adds experience-range constraints. Email uniqueness is enforced with a case-insensitive unique index.

## Index strategy

Implemented:

- UUID primary key
- Unique email expression index

Proposed:

- Default sort timestamps
- Stable cursor indexes
- Location and experience indexes
- Composite indexes for frequent combined filters

Indexes should follow measured query plans rather than assumptions.

## Pagination and query optimization

JPA Specifications perform database-side filtering. Spring Data JPA applies sorting and limits before mapping results.

Future improvements:

- Remove unpaginated `findAll()`.
- Add cursor/keyset pagination for deep pages.
- Use `Slice` if total counts are not needed.
- Add trigram/full-text indexes only if `%text%` search is measured as slow.

## Connection pooling

HikariCP is the Spring Boot default. Tune:

- Maximum pool size
- Connection timeout
- Idle timeout
- Maximum lifetime
- Statement timeout

Do not oversize pools; many replicas could exhaust PostgreSQL connections.

## Caching

Cache only measured hot reads such as candidate details or expensive filter counts.

Use:

- Redis or managed distributed cache
- Short TTL
- Explicit invalidation on writes
- Candidate UUID keys

Candidate personal data requires authorization-aware caching.

## Horizontal scaling

Run multiple stateless application replicas behind a load balancer.

Needed capabilities:

- Shared PostgreSQL
- External configuration
- Health/readiness checks
- Rolling deployments
- Distributed cache where needed
- Connection-pool awareness

## Read replicas

For read-heavy workloads:

- Route search and candidate reads to replicas.
- Keep writes and duplicate-email checks on the primary.
- Route strongly consistent reads to the primary.
- Account for replica lag.

## Partitioning

Partition only after sustained large-table issues:

- Range partition by creation time for retention
- Hash partition by key for storage distribution
- Partition strategy must match recurring queries or lifecycle needs

## Monitoring

Add:

- Structured logs
- Correlation IDs
- HTTP and database metrics
- Distributed traces
- PostgreSQL slow-query and resource metrics
- Connection pool metrics
- Alerts

Sensitive candidate fields and SQL bind values must not be logged.

## Security

Authentication and authorization remain proposed, not implemented.

Future controls:

- HTTPS
- Authenticated CRUD and search
- Role/resource authorization
- CORS policy
- Rate limits
- Request limits
- Least-privilege database accounts
- Secret management
- Audit logging
- Protected Swagger access

## Microservice decision

Keep the monolith until independent scaling, operational boundaries, or team ownership provide measurable benefit.
