package com.example.candidates.mapper;

import com.example.candidates.dto.CandidateRequest;
import com.example.candidates.dto.CandidateResponse;
import com.example.candidates.entity.Candidate;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CandidateMapper {

    public Candidate toEntity(CandidateRequest request) {
        return new Candidate(
                normalize(request.firstName()),
                normalize(request.lastName()),
                normalizeEmail(request.email()),
                normalizeOptional(request.phone()),
                normalizeOptional(request.location()),
                request.experienceYears(),
                normalizeOptional(request.resumeUrl())
        );
    }

    public void updateEntity(Candidate entity, CandidateRequest request) {
        entity.setFirstName(normalize(request.firstName()));
        entity.setLastName(normalize(request.lastName()));
        entity.setEmail(normalizeEmail(request.email()));
        entity.setPhone(normalizeOptional(request.phone()));
        entity.setLocation(normalizeOptional(request.location()));
        entity.setExperienceYears(request.experienceYears());
        entity.setResumeUrl(normalizeOptional(request.resumeUrl()));
    }

    public CandidateResponse toResponse(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getFirstName(),
                candidate.getLastName(),
                candidate.getEmail(),
                candidate.getPhone(),
                candidate.getLocation(),
                candidate.getExperienceYears(),
                candidate.getResumeUrl(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeOptional(email);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
