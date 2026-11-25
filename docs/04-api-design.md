# Candidate Management System — REST API Design

Base path: `/api/candidates`

## Create

`POST /api/candidates`

- Request: `CandidateRequest`
- Approved success: `201 Created`
- Include a `Location` header containing the candidate URL.
- Duplicate email: `409 Conflict`
- Validation failure: `400 Bad Request`

## Retrieve

`GET /api/candidates/{id}`

- Success: `200 OK`
- Invalid UUID: `400 Bad Request`
- Missing candidate: `404 Not Found`

## List, search, and filter

`GET /api/candidates`

Supported parameters:

| Parameter | Description |
|---|---|
| `search` | Partial case-insensitive search across approved text fields |
| `name` | Partial match across first and last name |
| `email` | Partial case-insensitive email match |
| `location` | Partial case-insensitive location match |
| `phone` | Optional partial phone match |
| `resumeUrl` | Optional partial resume URL match |
| `minExperience` | Inclusive minimum experience |
| `maxExperience` | Inclusive maximum experience |
| `page` | Zero-based page number |
| `size` | Page size, maximum 100 |
| `sortBy` | Allowlisted sort field |
| `sortDirection` | `asc` or `desc` |

Separate filters use `AND`. General text search uses `OR` across its selected fields.

## Update

`PUT /api/candidates/{id}`

- Request: `CandidateRequest`
- Success: `200 OK`
- Missing candidate: `404 Not Found`
- Validation failure: `400 Bad Request`
- Duplicate email: `409 Conflict`

## Delete

`DELETE /api/candidates/{id}`

- Success: `204 No Content`
- Missing candidate: `404 Not Found`

## Structured paginated response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "hasNext": false,
  "hasPrevious": false
}
```

An empty match set is `200 OK` with empty `content`, not `404`.

## Consistent error response

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "timestamp": "2026-01-01T00:00:00Z",
  "path": "/api/candidates",
  "fieldErrors": {
    "email": ["must be valid"]
  }
}
```
