package com.example.candidates.service.impl;

import com.example.candidates.dto.CandidateFilterRequest;
import com.example.candidates.dto.CandidateRequest;
import com.example.candidates.dto.CandidateResponse;
import com.example.candidates.dto.PageResponse;
import com.example.candidates.entity.Candidate;
import com.example.candidates.exception.DuplicateEmailException;
import com.example.candidates.exception.InvalidRequestException;
import com.example.candidates.exception.ResourceNotFoundException;
import com.example.candidates.mapper.CandidateMapper;
import com.example.candidates.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private CandidateMapper candidateMapper;

    private CandidateServiceImpl candidateService;

    @BeforeEach
    void setUp() {
        candidateService = new CandidateServiceImpl(candidateRepository, candidateMapper);
    }

    @Test
    void createsCandidateWhenEmailIsAvailable() {
        CandidateRequest request = request("Maya", "maya@example.com");
        Candidate entity = candidate("maya@example.com");
        CandidateResponse response = response(entity);
        when(candidateRepository.findByEmailIgnoreCase("maya@example.com"))
                .thenReturn(Optional.empty());
        when(candidateMapper.toEntity(request)).thenReturn(entity);
        when(candidateRepository.save(entity)).thenReturn(entity);
        when(candidateMapper.toResponse(entity)).thenReturn(response);

        CandidateResponse result = candidateService.create(request);

        assertThat(result).isSameAs(response);
        verify(candidateRepository).save(entity);
    }

    @Test
    void rejectsDuplicateEmailBeforeSaving() {
        CandidateRequest request = request("Other", "maya@example.com");
        Candidate existing = mock(Candidate.class);
        when(existing.getId()).thenReturn(UUID.randomUUID());
        when(candidateRepository.findByEmailIgnoreCase("maya@example.com"))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> candidateService.create(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    void retrievesCandidateById() {
        UUID id = UUID.randomUUID();
        Candidate entity = candidate("maya@example.com");
        CandidateResponse response = response(entity);
        when(candidateRepository.findById(id)).thenReturn(Optional.of(entity));
        when(candidateMapper.toResponse(entity)).thenReturn(response);

        CandidateResponse result = candidateService.getById(id);

        assertThat(result).isSameAs(response);
    }

    @Test
    void throwsNotFoundWhenRetrievingUnknownCandidate() {
        UUID id = UUID.randomUUID();
        when(candidateRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesCandidateWhenNewEmailIsAvailable() {
        UUID id = UUID.randomUUID();
        CandidateRequest request = request("Maya Updated", "updated@example.com");
        Candidate entity = candidate("old@example.com");
        CandidateResponse response = response(entity);
        when(candidateRepository.findById(id)).thenReturn(Optional.of(entity));
        when(candidateRepository.findByEmailIgnoreCase("updated@example.com"))
                .thenReturn(Optional.empty());
        when(candidateRepository.save(entity)).thenReturn(entity);
        when(candidateMapper.toResponse(entity)).thenReturn(response);

        CandidateResponse result = candidateService.update(id, request);

        assertThat(result).isSameAs(response);
        verify(candidateMapper).updateEntity(entity, request);
        verify(candidateRepository).save(entity);
    }

    @Test
    void rejectsUpdateWhenEmailBelongsToAnotherCandidate() {
        UUID id = UUID.randomUUID();
        Candidate current = candidate("current@example.com");
        Candidate other = mock(Candidate.class);
        when(other.getId()).thenReturn(UUID.randomUUID());
        CandidateRequest request = request("Maya", "other@example.com");
        when(candidateRepository.findById(id)).thenReturn(Optional.of(current));
        when(candidateRepository.findByEmailIgnoreCase("other@example.com"))
                .thenReturn(Optional.of(other));

        assertThatThrownBy(() -> candidateService.update(id, request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(candidateMapper, never()).updateEntity(any(Candidate.class), any());
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    void deletesExistingCandidate() {
        UUID id = UUID.randomUUID();
        Candidate entity = candidate("maya@example.com");
        when(candidateRepository.findById(id)).thenReturn(Optional.of(entity));

        candidateService.delete(id);

        verify(candidateRepository).delete(entity);
    }

    @Test
    void throwsNotFoundWhenDeletingUnknownCandidate() {
        UUID id = UUID.randomUUID();
        when(candidateRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(candidateRepository, never()).delete(any(Candidate.class));
    }

    @Test
    void returnsMappedPageAndUsesRequestedSort() {
        CandidateFilterRequest filters = new CandidateFilterRequest(
                null, null, null, null, null, null,
                1, 10, 0, 5, "lastName", "asc"
        );
        Candidate entity = candidate("maya@example.com");
        CandidateResponse response = response(entity);
        when(candidateRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(
                List.of(entity),
                PageRequest.of(0, 5, Sort.by("lastName").ascending()),
                6
        ));
        when(candidateMapper.toResponse(entity)).thenReturn(response);

        PageResponse<CandidateResponse> result = candidateService.search(filters);

        assertThat(result.content()).containsExactly(response);
        assertThat(result.totalElements()).isEqualTo(6);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        verify(candidateRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                eq(PageRequest.of(0, 5, Sort.by("lastName").ascending().and(Sort.by("id").ascending())))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidSearchFilters")
    void rejectsInvalidSearchFiltersBeforeQuerying(
            CandidateFilterRequest filters,
            String expectedMessage
    ) {
        assertThatThrownBy(() -> candidateService.search(filters))
                    .isInstanceOf(InvalidRequestException.class)
                .hasMessage(expectedMessage);

        verifyNoInteractions(candidateRepository, candidateMapper);
    }

    @ParameterizedTest
    @MethodSource("validOpenEndedExperienceFilters")
    void acceptsOpenEndedExperienceRanges(Integer minExperience, Integer maxExperience) {
        CandidateFilterRequest filters = filters(
                minExperience,
                maxExperience,
                null,
                null,
                null,
                null
        );
        when(candidateRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                any(Pageable.class)
        )).thenAnswer(invocation -> Page.empty(invocation.getArgument(1)));

        PageResponse<CandidateResponse> result = candidateService.search(filters);

        assertThat(result.content()).isEmpty();
        verify(candidateRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                any(Pageable.class)
        );
    }

    static Stream<Arguments> validOpenEndedExperienceFilters() {
        return Stream.of(
                Arguments.of(3, null),
                Arguments.of(null, 10)
        );
    }

    static Stream<Arguments> invalidSearchFilters() {
        return Stream.of(
                Arguments.of(filters(null, null, -1, null, null, null),
                        "Page must be zero or greater"),
                Arguments.of(filters(null, null, null, 0, null, null),
                        "Size must be between 1 and 100"),
                Arguments.of(filters(null, null, null, 101, null, null),
                        "Size must be between 1 and 100"),
                Arguments.of(filters(-1, null, null, null, null, null),
                        "Minimum experience cannot be negative"),
                Arguments.of(filters(null, -1, null, null, null, null),
                        "Maximum experience cannot be negative"),
                Arguments.of(filters(null, 51, 0, null, null, null),
                        "Experience filters must not exceed 50 years"),
                Arguments.of(filters(51, null, 0, null, null, null),
                        "Experience filters must not exceed 50 years"),
                Arguments.of(filters(8, 3, 0, null, null, null),
                        "Minimum experience cannot be greater than maximum experience"),
                Arguments.of(filters(null, null, null, null, "unknown", null),
                        "Unsupported candidate sort field"),
                Arguments.of(filters(null, null, null, null, null, "sideways"),
                        "Sort direction must be asc or desc")
        );
    }

    @Test
    void appliesDefaultPageSizeAndDescendingCreatedAtSort() {
        CandidateFilterRequest filters = filters(null, null, null, null, null, null);
        Pageable expected = PageRequest.of(
                0,
                20,
                Sort.by("createdAt").descending().and(Sort.by("id").ascending())
        );
        when(candidateRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                any(Pageable.class)
        )).thenReturn(Page.empty(expected));

        PageResponse<CandidateResponse> result = candidateService.search(filters);

        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        verify(candidateRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                eq(expected)
        );
    }

    @Test
    void appliesExplicitDescendingSortAndPagination() {
        CandidateFilterRequest filters = filters(null, null, 2, 10, " updatedAt ", " DESC ");
        Pageable expected = PageRequest.of(
                2,
                10,
                Sort.by("updatedAt").descending().and(Sort.by("id").ascending())
        );
        when(candidateRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                any(Pageable.class)
        )).thenReturn(Page.empty(expected));

        candidateService.search(filters);

        verify(candidateRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Candidate>>any(),
                eq(expected)
        );
    }

    private CandidateRequest request(String firstName, String email) {
        return new CandidateRequest(firstName, "Sharma", email, null, null, 5, null);
    }

    private static CandidateFilterRequest filters(
            Integer minExperience,
            Integer maxExperience,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        return new CandidateFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                minExperience,
                maxExperience,
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    private Candidate candidate(String email) {
        return new Candidate("Maya", "Sharma", email, null, null, 5, null);
    }

    private CandidateResponse response(Candidate candidate) {
        return new CandidateResponse(
                UUID.randomUUID(),
                candidate.getFirstName(),
                candidate.getLastName(),
                candidate.getEmail(),
                candidate.getPhone(),
                candidate.getLocation(),
                candidate.getExperienceYears(),
                candidate.getResumeUrl(),
                null,
                null
        );
    }
}
