package com.cap48.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record ValidationResultDto(
        String environment,
        String status,
        Instant checkedAt,
        JsonNode errors
) {
}
