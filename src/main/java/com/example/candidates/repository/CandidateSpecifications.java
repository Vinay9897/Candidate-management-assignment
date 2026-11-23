package com.example.candidates.repository;

import com.example.candidates.dto.CandidateFilterRequest;
import com.example.candidates.entity.Candidate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CandidateSpecifications {

    private CandidateSpecifications() {
    }

    public static Specification<Candidate> withFilters(CandidateFilterRequest filters) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addTextFilter(predicates, root, builder,
                    filters.search(),"firstName", "lastName", "email");

            addNameFilter(predicates, root, builder, filters.name());
            addContainsFilter(predicates, root, builder, "email", filters.email());
            addContainsFilter(predicates, root, builder, "location", filters.location());
            addContainsFilter(predicates, root, builder, "phone", filters.phone());
            addContainsFilter(predicates, root, builder, "resumeUrl", filters.resumeUrl());

            if (filters.minExperience() != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.get("experienceYears"), filters.minExperience()));
            }

            if (filters.maxExperience() != null) {
                predicates.add(builder.lessThanOrEqualTo(
                        root.get("experienceYears"), filters.maxExperience()));
            }

            return predicates.isEmpty()
                    ? builder.conjunction()
                    : builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addNameFilter(
            List<Predicate> predicates,
            jakarta.persistence.criteria.Root<Candidate> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            String value
    ) {
        String normalized = normalize(value);
        if (normalized == null) {
            return;
        }
        String pattern = containsPattern(normalized);
        predicates.add(builder.or(
                containsExpression(root, builder, "firstName", pattern),
                containsExpression(root, builder, "lastName", pattern)
        ));
    }

    private static void addTextFilter(
            List<Predicate> predicates,
            jakarta.persistence.criteria.Root<Candidate> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            String value,
            String... attributes
    ) {
        String normalized = normalize(value);
        if (normalized == null) {
            return;
        }
        String pattern = containsPattern(normalized);
        Predicate[] searchPredicates = new Predicate[attributes.length];
        for (int index = 0; index < attributes.length; index++) {
            searchPredicates[index] =
                    containsExpression(root, builder, attributes[index], pattern);
        }
        predicates.add(builder.or(searchPredicates));
    }

    private static void addContainsFilter(
            List<Predicate> predicates,
            jakarta.persistence.criteria.Root<Candidate> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            String attribute,
            String value
    ) {
        String normalized = normalize(value);
        if (normalized == null) {
            return;
        }
        predicates.add(containsExpression(
                root,
                builder,
                attribute,
                containsPattern(normalized)
        ));
    }

    private static Predicate containsExpression(
            jakarta.persistence.criteria.Root<Candidate> root,
            jakarta.persistence.criteria.CriteriaBuilder builder,
            String attribute,
            String pattern
    ) {
        return builder.like(
                builder.lower(root.get(attribute)),
                pattern,
                '\\'
        );
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty()
                ? null
                : normalized.toLowerCase(Locale.ROOT);
    }

    private static String containsPattern(String value) {
        return "%" + escapeLike(value) + "%";
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
