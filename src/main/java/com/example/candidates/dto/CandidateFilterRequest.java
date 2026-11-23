package com.example.candidates.dto;

public record CandidateFilterRequest(
        String name,
        String email,
        String location,
        String phone,
        String resumeUrl,
        String search,
        Integer minExperience,
        Integer maxExperience,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
}
