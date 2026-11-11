package com.cap48.web.dto;

import java.time.Instant;
import java.util.List;

public record DriftMatrixDto(Instant comparedAt, List<DriftPairDto> pairs) {

    public record DriftPairDto(String envA, String envB, boolean hasDrift, long driftCount) {
    }
}
