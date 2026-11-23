package com.example.candidates.service;

import com.example.candidates.dto.CandidateRequest;
import com.example.candidates.dto.CandidateResponse;
import com.example.candidates.dto.CandidateFilterRequest;
import com.example.candidates.dto.PageResponse;

import java.util.List;
import java.util.UUID;

public interface CandidateService {

    CandidateResponse create(CandidateRequest request);

    CandidateResponse getById(UUID id);

    List<CandidateResponse> getAll();

    CandidateResponse update(UUID id, CandidateRequest request);

    void delete(UUID id);

    PageResponse<CandidateResponse> search(CandidateFilterRequest filters);
}
