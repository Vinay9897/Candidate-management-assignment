package com.example.candidates.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

public record CandidateRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 32)
        @Pattern(
                regexp = "^(?=(?:[^0-9]*[0-9]){7,15}[^0-9]*$)\\+?[0-9][0-9 ()-]*[0-9]$",
                message = "must contain 7 to 15 digits and use only an optional leading +, spaces, parentheses, or hyphens"
        )
        String phone,
        @Size(max = 160) String location,
        @Min(0) @Max(50) Integer experienceYears,
        @Size(max = 2048) @URL String resumeUrl
) {
}
