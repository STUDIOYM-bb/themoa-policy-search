package com.themoa.policysearch.policy.search.controller;

import com.themoa.policysearch.common.response.ApiResponse;
import com.themoa.policysearch.member.service.CurrentMemberProvider;
import com.themoa.policysearch.policy.search.dto.PolicyDetailResponse;
import com.themoa.policysearch.policy.search.dto.PolicySearchRequest;
import com.themoa.policysearch.policy.search.dto.PolicySearchResponse;
import com.themoa.policysearch.policy.search.service.PolicySearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
public class PolicySearchController {
    private final PolicySearchService searchService;
    private final CurrentMemberProvider currentMemberProvider;

    public PolicySearchController(PolicySearchService searchService, CurrentMemberProvider currentMemberProvider) {
        this.searchService = searchService;
        this.currentMemberProvider = currentMemberProvider;
    }

    @PostMapping("/search")
    public ApiResponse<PolicySearchResponse> search(@Valid @RequestBody PolicySearchRequest request) {
        Integer memberId = null;
        try {
            memberId = currentMemberProvider.currentMember().getId();
        } catch (RuntimeException ignored) {
            memberId = null;
        }
        return ApiResponse.ok(searchService.search(request, memberId));
    }

    @GetMapping("/{policyId}")
    public ApiResponse<PolicyDetailResponse> detail(@PathVariable Integer policyId) {
        return ApiResponse.ok(searchService.detail(policyId));
    }
}
