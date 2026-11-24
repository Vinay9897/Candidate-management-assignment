package com.example.candidates;

import com.example.candidates.dto.CandidateRequest;
import com.example.candidates.repository.CandidateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=${CANDIDATE_DB_URL:jdbc:postgresql://localhost:5432/candidate_management}",
        "spring.datasource.username=${CANDIDATE_DB_USERNAME}",
        "spring.datasource.password=${CANDIDATE_DB_PASSWORD}",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class CandidateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidateRepository candidateRepository;

    @BeforeEach
    void clearCandidates() {
        candidateRepository.deleteAll();
    }

    @Test
    void createGetUpdateAndDeleteCandidate() throws Exception {
        CandidateRequest createRequest = validRequest("Maya", "maya@example.com");

        String response = mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.firstName").value("Maya"))
                .andExpect(jsonPath("$.email").value("maya@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID id = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(get("/api/candidates/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        mockMvc.perform(get("/api/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));

        CandidateRequest updateRequest = validRequest("Maya Updated", "maya.updated@example.com");
        mockMvc.perform(put("/api/candidates/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Maya Updated"))
                .andExpect(jsonPath("$.email").value("maya.updated@example.com"));

        mockMvc.perform(delete("/api/candidates/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/candidates/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsValidationAndDuplicateEmail() throws Exception {
        CandidateRequest invalid = new CandidateRequest(
                "",
                "",
                "not-an-email",
                null,
                null,
                -1,
                null
        );

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.lastName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        CandidateRequest first = validRequest("Alex", "alex@example.com");
        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                validRequest("Other", "ALEX@EXAMPLE.COM")
                        )))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsNotFoundForUnknownIdAndBadRequestForMalformedId() throws Exception {
        mockMvc.perform(get("/api/candidates/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/candidates/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void combinesSearchFiltersPaginationAndSorting() throws Exception {
        CandidateRequest maya = validRequest("Maya", "maya@example.com");
        CandidateRequest manoj = validRequest("Manoj", "manoj@example.com");

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maya)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(manoj)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/candidates")
                        .param("search", "MA")
                        .param("location", "bang")
                        .param("minExperience", "3")
                        .param("maxExperience", "6")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].location").value("Bangalore"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/api/candidates")
                        .param("email", "does-not-exist")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/candidates")
                        .param("minExperience", "8")
                        .param("maxExperience", "3"))
                .andExpect(status().isBadRequest());
    }

    private CandidateRequest validRequest(String firstName, String email) {
        return new CandidateRequest(
                firstName,
                "Sharma",
                email,
                null,
                "Bangalore",
                5,
                null
        );
    }
}
