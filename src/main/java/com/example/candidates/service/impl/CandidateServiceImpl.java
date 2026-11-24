package com.example.candidates.service.impl;

import com.example.candidates.dto.CandidateRequest;
import com.example.candidates.dto.CandidateResponse;
import com.example.candidates.dto.CandidateFilterRequest;
import com.example.candidates.dto.PageResponse;
import com.example.candidates.entity.Candidate;
import com.example.candidates.exception.DuplicateEmailException;
import com.example.candidates.exception.InvalidRequestException;
import com.example.candidates.exception.ResourceNotFoundException;
import com.example.candidates.mapper.CandidateMapper;
import com.example.candidates.repository.CandidateRepository;
import com.example.candidates.repository.CandidateSpecifications;
import com.example.candidates.service.CandidateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;

@Service
@Transactional
public class CandidateServiceImpl implements CandidateService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private static final Map<String, String> SORT_FIELDS = Map.ofEntries(
            Map.entry("firstName", "firstName"),
            Map.entry("lastName", "lastName"),
            Map.entry("email", "email"),
            Map.entry("location", "location"),
            Map.entry("experienceYears", "experienceYears"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private final CandidateRepository candidateRepository;
    private final CandidateMapper candidateMapper;

    public CandidateServiceImpl(CandidateRepository candidateRepository,
                                CandidateMapper candidateMapper) {
        this.candidateRepository = candidateRepository;
        this.candidateMapper = candidateMapper;
    }

    @Override
    public CandidateResponse create(CandidateRequest request) {
        String email = normalizeEmail(request.email());
        rejectDuplicateEmail(email, null);

        Candidate candidate = candidateMapper.toEntity(request);
        return candidateMapper.toResponse(candidateRepository.save(candidate));
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getById(UUID id) {
        Candidate candidate = findCandidate(id);
        return candidateMapper.toResponse(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateResponse> getAll() {
        return candidateRepository.findAll().stream()
                .map(candidateMapper::toResponse)
                .toList();
    }

    @Override
    public CandidateResponse update(UUID id, CandidateRequest request) {
        Candidate candidate = findCandidate(id);
        String email = normalizeEmail(request.email());
        rejectDuplicateEmail(email, id);

        candidateMapper.updateEntity(candidate, request);
        return candidateMapper.toResponse(candidateRepository.save(candidate));
    }

    @Override
    public void delete(UUID id) {
        Candidate candidate = findCandidate(id);
        candidateRepository.delete(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CandidateResponse> search(CandidateFilterRequest filters) {
        validateFilters(filters);
        Pageable pageable = buildPageable(filters);
        Page<Candidate> candidatePage = candidateRepository.findAll(
                CandidateSpecifications.withFilters(filters),
                pageable
        );

        List<CandidateResponse> content = candidatePage.getContent().stream()
                .map(candidateMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                candidatePage.getNumber(),
                candidatePage.getSize(),
                candidatePage.getTotalElements(),
                candidatePage.getTotalPages(),
                candidatePage.hasNext(),
                candidatePage.hasPrevious()
        );
    }

    private void validateFilters(CandidateFilterRequest filters) {
        Integer page = filters.page();
        Integer size = filters.size();
        Integer minExperience = filters.minExperience();
        Integer maxExperience = filters.maxExperience();

        if (page != null && page < 0) {
            throw new InvalidRequestException("Page must be zero or greater");
        }
        if (size != null && (size < 1 || size > MAX_SIZE)) {
            throw new InvalidRequestException("Size must be between 1 and " + MAX_SIZE);
        }
        if (minExperience != null && minExperience < 0) {
            throw new InvalidRequestException("Minimum experience cannot be negative");
        }
        if (maxExperience != null && maxExperience < 0) {
            throw new InvalidRequestException("Maximum experience cannot be negative");
        }
        if ((minExperience != null && minExperience > 50)
                || (maxExperience != null && maxExperience > 50)) {
            throw new InvalidRequestException("Experience filters must not exceed 50 years");
        }
        if (minExperience != null && maxExperience != null
                && minExperience > maxExperience) {
            throw new InvalidRequestException(
                    "Minimum experience cannot be greater than maximum experience"
            );
        }

        if (filters.sortBy() != null
                && !SORT_FIELDS.containsKey(filters.sortBy().trim())) {
            throw new InvalidRequestException("Unsupported candidate sort field");
        }

        if (filters.sortDirection() != null) {
            String direction = filters.sortDirection().trim().toLowerCase(Locale.ROOT);
            if (!direction.equals("asc") && !direction.equals("desc")) {
                throw new InvalidRequestException(
                        "Sort direction must be asc or desc"
                );
            }
        }
    }

    private Pageable buildPageable(CandidateFilterRequest filters) {
        int page = filters.page() == null ? DEFAULT_PAGE : filters.page();
        int size = filters.size() == null ? DEFAULT_SIZE : filters.size();

        String sortBy = filters.sortBy() == null
                ? "createdAt"
                : filters.sortBy().trim();
        String sortDirection = filters.sortDirection() == null
                ? "desc"
                : filters.sortDirection().trim();

        Sort sort = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.by(SORT_FIELDS.getOrDefault(sortBy, "createdAt")).ascending()
                : Sort.by(SORT_FIELDS.getOrDefault(sortBy, "createdAt")).descending();
        sort = sort.and(Sort.by("id").ascending());

        return PageRequest.of(page, size, sort);
    }

    private Candidate findCandidate(UUID id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with id: " + id
                ));
    }

    private void rejectDuplicateEmail(String email, UUID currentCandidateId) {
        candidateRepository.findByEmailIgnoreCase(email)
                .filter(candidate -> !candidate.getId().equals(currentCandidateId))
                .ifPresent(candidate -> {
                    throw new DuplicateEmailException(
                            "A candidate with this email already exists"
                    );
                });
    }

    private String normalizeEmail(String email) {
        return email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }
}
