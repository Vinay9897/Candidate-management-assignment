package com.example.candidates.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record CandidateFilterRequest(
        String name,
        String email,
        String location,
        String phone,
        String resumeUrl,
        String search,
        @Min(0) @Max(50) Integer minExperience,
        @Min(0) @Max(50) Integer maxExperience,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        @Pattern(
                regexp = "firstName|lastName|email|location|experienceYears|createdAt|updatedAt",
                message = "must be a supported candidate sort field"
        ) String sortBy,
        @Pattern(regexp = "(?i)asc|desc", message = "must be asc or desc")
        String sortDirection
) {
}
