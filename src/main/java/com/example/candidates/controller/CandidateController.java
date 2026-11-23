package com.example.candidates.controller;

import com.example.candidates.dto.CandidateRequest;
import com.example.candidates.dto.CandidateResponse;
import com.example.candidates.dto.CandidateFilterRequest;
import com.example.candidates.dto.PageResponse;
import com.example.candidates.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

//import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> create(
            @Valid @RequestBody CandidateRequest request
    ) {
        CandidateResponse created = candidateService.create(request);
        /*URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
                */
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(candidateService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CandidateResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String resumeUrl,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        CandidateFilterRequest filters = new CandidateFilterRequest(
                name,
                email,
                location,
                phone,
                resumeUrl,
                search,
                minExperience,
                maxExperience,
                page,
                size,
                sortBy,
                sortDirection
        );
        return ResponseEntity.ok(candidateService.search(filters));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CandidateRequest request
    ) {
        return ResponseEntity.ok(candidateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
