package com.themoa.policysearch.policy.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record PolicySearchRequest(
        @NotBlank @Size(min = 2, max = 300) String query,
        Map<String, String> supplementalConditions,
        @Min(0) int page,
        @Min(1) @Max(50) int size
) {
}
