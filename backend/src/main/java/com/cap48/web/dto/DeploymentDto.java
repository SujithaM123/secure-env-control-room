package com.cap48.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record DeploymentDto(
        Long id,
        String environment,
        String imageVersion,
        Instant deployedAt,
        boolean verified,
        JsonNode verificationDetails
) {
}
