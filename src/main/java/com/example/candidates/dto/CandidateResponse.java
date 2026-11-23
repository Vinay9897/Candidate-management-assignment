package com.example.candidates.dto;

import java.time.Instant;
import java.util.UUID;

public record CandidateResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String location,
        Integer experienceYears,
        String resumeUrl,
        Instant createdAt,
        Instant updatedAt
) {
}
