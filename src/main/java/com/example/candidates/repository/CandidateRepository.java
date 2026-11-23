package com.example.candidates.repository;

import com.example.candidates.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID>,
        JpaSpecificationExecutor<Candidate> {

    Optional<Candidate> findByEmailIgnoreCase(String email);
}
