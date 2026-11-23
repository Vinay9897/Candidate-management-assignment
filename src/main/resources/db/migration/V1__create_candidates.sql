CREATE TABLE candidates (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(32),
    location VARCHAR(160),
    experience_years INTEGER,
    resume_url VARCHAR(2048),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT candidates_first_name_not_blank CHECK (BTRIM(first_name) <> ''),
    CONSTRAINT candidates_last_name_not_blank CHECK (BTRIM(last_name) <> ''),
    CONSTRAINT candidates_email_not_blank CHECK (BTRIM(email) <> ''),
    CONSTRAINT candidates_experience_years_non_negative CHECK (
        experience_years IS NULL OR experience_years >= 0
    )
);

CREATE UNIQUE INDEX candidates_email_lower_unique
    ON candidates (LOWER(BTRIM(email)));
