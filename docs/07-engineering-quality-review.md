# Candidate Management System — Engineering Quality Review

## Critical findings

1. **Public APIs**
   - Candidate CRUD and search endpoints are accessible without authentication or authorization.
   - Authentication and authorization require approval before implementation.

2. **POST status contract**
   - POST currently returns `200 OK`.
   - The approved contract requires `201 Created` with a `Location` header.

3. **Unpaginated list**
   - `CandidateServiceImpl.getAll()` uses `CandidateRepository.findAll()`.
   - It can load all candidate records and should be removed or replaced by paginated search.

## Recommended improvements

- Add application logs for lifecycle events using candidate IDs, not candidate personal data.
- Do not log names, emails, phone numbers, resume URLs, SQL bind values, or credentials.
- Restrict Swagger UI outside development environments.
- Add rate limiting and request size limits for public deployments.
- Review SQL execution plans and add measured indexes.
- Move `@Transactional` to write methods and mark read/search operations read-only.
- Remove duplicate experience validation where it duplicates the wrong responsibility.
- Add a correlation/request ID to logs and error responses.
- Restore `GET /api/candidates` as the only paginated list/search endpoint.
- Add endpoint operation and parameter documentation.

## Optional improvements

- Structured JSON logging.
- Datasource and request metrics.
- Slow-query monitoring.
- Cursor pagination after offset pagination becomes slow.
- Trigram or full-text indexes for measured search workloads.
- Caching for hot candidate detail reads.

## N+1 status

No N+1 problem is currently present because candidate entities have scalar fields and no loaded skills/status collections.
