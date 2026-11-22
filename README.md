# Candidate Management System

Backend service for candidate CRUD, search, filtering, and persistence.

## Requirements

- Java 21
- Maven 3.9+
- PostgreSQL

## Configuration

Configure the database using environment variables:

| Variable | Required | Default |
|---|---:|---|
| `CANDIDATE_DB_URL` | No | `jdbc:postgresql://localhost:5432/candidate_management` |
| `CANDIDATE_DB_USERNAME` | Yes | None |
| `CANDIDATE_DB_PASSWORD` | Yes | None |

Do not commit database credentials. Create the database before running the application.

## Build and test

```powershell
mvn clean verify
```

PostgreSQL integration tests use Testcontainers and require Docker when those tests are added.

## Run locally

Set the database username and password in the environment, then run:

```powershell
mvn spring-boot:run
```

OpenAPI documentation is available at `/swagger-ui.html`.

## Database migrations

Flyway migrations belong in `src/main/resources/db/migration`.
