package com.example.candidates;

import com.example.candidates.controller.CandidateController;
import com.example.candidates.dto.CandidateFilterRequest;
import com.example.candidates.dto.CandidateRequest;
import com.example.candidates.dto.CandidateResponse;
import com.example.candidates.dto.PageResponse;
import com.example.candidates.exception.DuplicateEmailException;
import com.example.candidates.exception.GlobalExceptionHandler;
import com.example.candidates.exception.ResourceNotFoundException;
import com.example.candidates.service.CandidateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CandidateController.class)
@Import(GlobalExceptionHandler.class)
class CandidateValidationWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CandidateService candidateService;

    @Test
    void reportsBodyFieldValidationWithConsistentErrorShape() throws Exception {
        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": " ",
                                  "lastName": "",
                                  "email": "not-an-email",
                                  "phone": "123-abc",
                                  "experienceYears": 51
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/candidates"))
                .andExpect(jsonPath("$.fieldErrors.firstName").isArray())
                .andExpect(jsonPath("$.fieldErrors.email").isArray())
                .andExpect(jsonPath("$.fieldErrors.phone").isArray())
                .andExpect(jsonPath("$.fieldErrors.experienceYears").isArray());

        verifyNoInteractions(candidateService);
    }

    @Test
    void rejectsInvalidSearchParameters() throws Exception {
        mockMvc.perform(get("/api/candidates")
                        .param("page", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/candidates"));

        mockMvc.perform(get("/api/candidates")
                        .param("minExperience", "0")
                        .param("maxExperience", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.maxExperience").isArray());

        verifyNoInteractions(candidateService);
    }

    @Test
    void bindsValidSearchParametersAndReturnsPagedResults() throws Exception {
        when(candidateService.search(any(CandidateFilterRequest.class)))
                .thenReturn(new PageResponse<>(List.<CandidateResponse>of(), 0, 10, 0, 0, false, false));

        mockMvc.perform(get("/api/candidates")
                        .param("name", "Maya")
                        .param("minExperience", "2")
                        .param("maxExperience", "50")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.size").value(10));

        verify(candidateService).search(argThat(filters ->
                "Maya".equals(filters.name())
                        && filters.minExperience() == 2
                        && filters.maxExperience() == 50
                        && filters.page() == 0
                        && filters.size() == 10
                        && "firstName".equals(filters.sortBy())
                        && "asc".equals(filters.sortDirection())
        ));
    }

    @Test
    void mapsCandidateErrorsToSafeResponses() throws Exception {
        UUID id = UUID.randomUUID();
        when(candidateService.getById(id))
                .thenThrow(new ResourceNotFoundException("internal candidate lookup detail"));

        mockMvc.perform(get("/api/candidates/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Candidate not found"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("internal")
                )))
                .andExpect(jsonPath("$.path").value("/api/candidates/" + id));
    }

    @Test
    void hidesDatabaseFailureDetailsFromClients() throws Exception {
        UUID id = UUID.randomUUID();
        when(candidateService.getById(id))
                .thenThrow(new DataAccessResourceFailureException("database-password"));

        mockMvc.perform(get("/api/candidates/" + id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("database-password")
                )));
    }

    @Test
    void mapsDuplicateEmailToConflictWithoutInternalDetails() throws Exception {
        when(candidateService.create(any()))
                .thenThrow(new DuplicateEmailException("duplicate constraint details"));

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ari",
                                  "lastName": "Lee",
                                  "email": "ari@example.com",
                                  "phone": "+1 (202) 555-0123",
                                  "experienceYears": 50
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A candidate with this email already exists"));
    }

    @Test
    void createsCandidateUsingApprovedCreatedResponseContract() throws Exception {
        UUID id = UUID.randomUUID();
        CandidateResponse response = candidateResponse(id, "Maya", "maya@example.com");
        when(candidateService.create(any(CandidateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Maya",
                                  "lastName": "Sharma",
                                  "email": "maya@example.com",
                                  "phone": "+1 (202) 555-0123",
                                  "experienceYears": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        org.hamcrest.Matchers.endsWith(id.toString())
                ))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("maya@example.com"));
    }

    @Test
    void retrievesCandidateSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        when(candidateService.getById(id))
                .thenReturn(candidateResponse(id, "Maya", "maya@example.com"));

        mockMvc.perform(get("/api/candidates/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Maya"));
    }

    @Test
    void updatesCandidateSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        when(candidateService.update(eq(id), any(CandidateRequest.class)))
                .thenReturn(candidateResponse(id, "Maya Updated", "updated@example.com"));

        mockMvc.perform(put("/api/candidates/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Maya Updated",
                                  "lastName": "Sharma",
                                  "email": "updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Maya Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void deletesCandidateSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(candidateService).delete(id);

        mockMvc.perform(delete("/api/candidates/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnsEmptyPageWhenSearchHasNoMatches() throws Exception {
        when(candidateService.search(any(CandidateFilterRequest.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false, false));

        mockMvc.perform(get("/api/candidates").param("email", "missing@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    private CandidateResponse candidateResponse(UUID id, String firstName, String email) {
        return new CandidateResponse(
                id,
                firstName,
                "Sharma",
                email,
                null,
                "Bangalore",
                5,
                null,
                null,
                null
        );
    }
}
