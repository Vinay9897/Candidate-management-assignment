package com.example.candidates.repository;

import com.example.candidates.entity.Candidate;
import com.example.candidates.dto.CandidateFilterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=${CANDIDATE_DB_URL:jdbc:postgresql://localhost:5432/candidate_management}",
        "spring.datasource.username=${CANDIDATE_DB_USERNAME}",
        "spring.datasource.password=${CANDIDATE_DB_PASSWORD}",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CandidateRepositoryTest {

    @Autowired
    private CandidateRepository candidateRepository;

    @Test
    void savesAndReadsCandidate() {
        Candidate candidate = new Candidate(
                "Maya",
                "Sharma",
                email("maya"),
                null,
                "Bangalore",
                5,
                null
        );

        Candidate saved = candidateRepository.saveAndFlush(candidate);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(candidateRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findsCandidateByEmailIgnoringCase() {
        String candidateEmail = email("alex");
        Candidate candidate = new Candidate(
                "Alex",
                "Patel",
                candidateEmail,
                null,
                null,
                null,
                null
        );
        candidateRepository.saveAndFlush(candidate);

        assertThat(candidateRepository.findByEmailIgnoreCase(candidateEmail.toUpperCase()))
                .isPresent();
    }

    @Test
    void appliesPartialCaseInsensitiveFiltersAndCombinesCriteria() {
        String locationMarker = "filter-" + UUID.randomUUID();
        Candidate matching = candidate("Alice", "Smith", "New York " + locationMarker, 4, "alice");
        Candidate wrongExperience = candidate("Alicia", "Jones", "New York " + locationMarker,
                12, "alicia");
        Candidate wrongLocation = candidate("Alice", "Brown", "Bangalore", 4, "alice-b");
        candidateRepository.saveAllAndFlush(List.of(matching, wrongExperience, wrongLocation));

        CandidateFilterRequest filters = filters("ALI", locationMarker, 3, 6);
        List<Candidate> results = candidateRepository.findAll(
                CandidateSpecifications.withFilters(filters)
        );

        assertThat(results)
                .extracting(Candidate::getEmail)
                .containsExactly(matching.getEmail());
    }

    @Test
    void supportsDatabasePaginationAndSorting() {
        String locationMarker = "page-" + UUID.randomUUID();
        candidateRepository.saveAllAndFlush(List.of(
                candidate("Maya", "Sharma", locationMarker, 5, "maya"),
                candidate("Alice", "Smith", locationMarker, 2, "alice-sort"),
                candidate("Charles", "Lee", locationMarker, 8, "charles")
        ));

        Page<Candidate> firstPage = candidateRepository.findAll(
                CandidateSpecifications.withFilters(filters(null, locationMarker, null, null)),
                PageRequest.of(0, 2, Sort.by("firstName").ascending())
        );
        Page<Candidate> secondPage = candidateRepository.findAll(
                CandidateSpecifications.withFilters(filters(null, locationMarker, null, null)),
                PageRequest.of(1, 2, Sort.by("firstName").ascending())
        );

        assertThat(firstPage.getContent())
                .extracting(Candidate::getFirstName)
                .containsExactly("Alice", "Charles");
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.getContent())
                .extracting(Candidate::getFirstName)
                .containsExactly("Maya");
    }

    @Test
    void returnsEmptyPageWhenSpecificationDoesNotMatch() {
        candidateRepository.saveAndFlush(
                candidate("Maya", "Sharma", "Bangalore", 5, "maya-empty")
        );
        CandidateFilterRequest filters = new CandidateFilterRequest(
                null, "missing-" + UUID.randomUUID(), null, null, null, null,
                null, null, 0, 10, null, null
        );

        Page<Candidate> result = candidateRepository.findAll(
                CandidateSpecifications.withFilters(filters),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private Candidate candidate(
            String firstName,
            String lastName,
            String location,
            int experience,
            String emailPrefix
    ) {
        return new Candidate(
                firstName,
                lastName,
                email(emailPrefix),
                null,
                location,
                experience,
                null
        );
    }

    private String email(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.test";
    }

    private CandidateFilterRequest filters(
            String name,
            String location,
            Integer minExperience,
            Integer maxExperience
    ) {
        return new CandidateFilterRequest(
                name,
                null,
                location,
                null,
                null,
                null,
                minExperience,
                maxExperience,
                0,
                20,
                null,
                null
        );
    }

}
