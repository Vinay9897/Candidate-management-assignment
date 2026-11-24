package com.example.candidates.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ApiError(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path,
        Map<String, List<String>> fieldErrors
) {
}
