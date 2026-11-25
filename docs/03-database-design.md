# Candidate Management System — Database Design

## Table: `candidates`

| Column | PostgreSQL type | Constraint |
|---|---|---|
| `id` | `UUID` | Primary key |
| `first_name` | `VARCHAR(100)` | Not null |
| `last_name` | `VARCHAR(100)` | Not null |
| `email` | `VARCHAR(254)` | Not null |
| `phone` | `VARCHAR(32)` | Nullable |
| `location` | `VARCHAR(160)` | Nullable |
| `experience_years` | `INTEGER` | 0–50 when supplied |
| `resume_url` | `VARCHAR(2048)` | Nullable |
| `created_at` | `TIMESTAMPTZ` | Not null |
| `updated_at` | `TIMESTAMPTZ` | Not null |

## Constraints

- First name and last name must contain non-whitespace characters.
- Email must contain non-whitespace characters.
- Email uniqueness is case-insensitive and ignores surrounding whitespace.
- Experience is null or between 0 and 50.

The unique index is:

```sql
CREATE UNIQUE INDEX candidates_email_lower_unique
    ON candidates (LOWER(BTRIM(email)));
```

## Migration history

- `V1__create_candidates.sql` creates the candidate table and baseline constraints.
- `V2__limit_candidate_experience.sql` replaces the baseline non-negative constraint with a 0–50 range.

## Index strategy

Implemented:

- UUID primary key lookup.
- Unique case-insensitive email index.

Future indexes should be based on measured query plans:

- `created_at`
- `(created_at, id)` for stable pagination
- `first_name`
- `last_name`
- `location`
- `experience_years`
- `(location, experience_years)` if commonly combined

Leading-wildcard text searches are not automatically efficient with ordinary B-tree indexes. PostgreSQL trigram or full-text indexes may be introduced if profiling shows a need.
