# Candidate Management System

Backend service for candidate CRUD, search, filtering, and persistence.

## Package structure

Application code is organized by layer under `com.example.candidates`:

- `controller` — REST endpoints
- `service` and `service.impl` — use cases and service implementation
- `repository` — persistence access
- `entity` — JPA entities
- `dto` — API request and response types
- `exception` — application exceptions and HTTP error handling
- `mapper` — entity-to-DTO mapping

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

For local development, credentials can also be entered in
`src/main/resources/application-local.properties`:

```properties
spring.datasource.username=your-postgresql-username
spring.datasource.password=your-postgresql-password
```

The local properties file is ignored by Git and should not be added to version
control. Fill in both values before starting the application.

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
