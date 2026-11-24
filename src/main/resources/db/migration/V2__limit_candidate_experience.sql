ALTER TABLE candidates
    DROP CONSTRAINT candidates_experience_years_non_negative;

ALTER TABLE candidates
    ADD CONSTRAINT candidates_experience_years_range
    CHECK (experience_years IS NULL OR experience_years BETWEEN 0 AND 50);
