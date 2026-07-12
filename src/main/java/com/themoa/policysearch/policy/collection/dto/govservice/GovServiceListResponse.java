package com.themoa.policysearch.policy.collection.dto.govservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GovServiceListResponse(
        Integer page,
        Integer perPage,
        Integer totalCount,
        Integer currentCount,
        Integer matchCount,
        List<GovServiceListItem> data
) {
}
