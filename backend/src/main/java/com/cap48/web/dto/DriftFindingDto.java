package com.cap48.web.dto;

public record DriftFindingDto(
        String envA,
        String envB,
        String settingKey,
        String valueA,
        String valueB,
        boolean allowed
) {
}
