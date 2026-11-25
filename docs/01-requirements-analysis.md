# Candidate Management System — Requirements Analysis

## Functional requirements

- Create candidate information.
- Retrieve a candidate by ID.
- List candidates.
- Update candidate information.
- Delete candidate information.
- Search candidates.
- Combine multiple optional filters.
- Validate invalid input and return meaningful errors.
- Persist candidate information across application restarts.
- Provide automated tests.

## Mandatory candidate fields

The implementation uses these mandatory fields:

| Field | Purpose |
|---|---|
| `id` | Unique system-generated candidate identifier |
| `firstName` | Candidate identification and text search |
| `lastName` | Candidate identification and text search |
| `email` | Candidate contact and unique lookup |

## Optional candidate fields

| Field | Purpose |
|---|---|
| `phone` | Candidate contact |
| `location` | Location filtering |
| `experienceYears` | Experience filtering |
| `resumeUrl` | Resume reference |

Audit timestamps, `createdAt` and `updatedAt`, are system-generated.

## Validation rules

- Names: required, non-blank, maximum 100 characters.
- Email: required, valid format, maximum 254 characters.
- Phone: optional, maximum 32 characters, 7–15 digits, optional leading `+`, and common separators.
- Location: optional, maximum 160 characters.
- Experience: optional integer between 0 and 50.
- Resume URL: optional valid HTTP/HTTPS-style URL, maximum 2048 characters.
- IDs: valid UUID values.

## REST contract

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/candidates` | Create |
| `GET` | `/api/candidates/{id}` | Retrieve |
| `GET` | `/api/candidates` | List, search, and filter |
| `PUT` | `/api/candidates/{id}` | Replace/update |
| `DELETE` | `/api/candidates/{id}` | Delete |

The approved implementation contract requires POST to return `201 Created` with a `Location` header.

## Search and filtering

- Text matching is partial and case-insensitive where appropriate.
- Separate filters are combined with `AND`.
- General text search uses `OR` across approved text fields.
- Results support pagination and sorting.
- Empty results return a successful paginated response with an empty `content` array.

## Assumptions to confirm

- Authentication and authorization are not required by the assignment.
- Status and skills are not included in the baseline data model.
- Email uniqueness is enforced.
- Experience is limited to 0–50 years.
- Candidate deletion is permanent.
