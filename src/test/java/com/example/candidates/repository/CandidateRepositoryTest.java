package com.example.candidates.repository;

import com.example.candidates.entity.Candidate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
                "maya@example.com",
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
        Candidate candidate = new Candidate(
                "Alex",
                "Patel",
                "alex@example.com",
                null,
                null,
                null,
                null
        );
        candidateRepository.saveAndFlush(candidate);

        assertThat(candidateRepository.findByEmailIgnoreCase("ALEX@EXAMPLE.COM"))
                .isPresent();
    }
}
