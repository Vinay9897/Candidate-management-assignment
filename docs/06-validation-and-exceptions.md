# Candidate Management System — Validation and Exception Handling

## Request validation

Jakarta Bean Validation is applied to `CandidateRequest` and filtering/query DTOs using `@Valid`.

Validated values include:

- Mandatory names and email
- Email format
- Phone format
- Optional field lengths
- Experience range
- Page number
- Page size
- Sort field allowlist
- Sort direction

## Phone format

The approved format allows:

- Optional leading `+`
- 7–15 digits
- Spaces
- Parentheses
- Hyphens

## Experience range

Candidate experience must be null or from 0 through 50 inclusive.

This rule is enforced by:

- Bean Validation
- Service cross-field checks
- The PostgreSQL check constraint

## Centralized exception handling

`@RestControllerAdvice` handles:

- Candidate not found
- Duplicate email
- Invalid request
- Bean and binding validation
- Malformed JSON
- Invalid UUID or parameter types
- Database integrity violations
- Database access failures
- Unexpected server exceptions

## Safe API responses

Responses do not expose:

- SQL text
- Stack traces
- Constraint names
- Database credentials
- Internal exception messages

Database and unexpected server errors are logged server-side and returned as generic `500` messages.
