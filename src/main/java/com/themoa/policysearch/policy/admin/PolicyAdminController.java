package com.themoa.policysearch.policy.admin;

import com.themoa.policysearch.common.response.ApiResponse;
import com.themoa.policysearch.common.security.AdminKeyValidator;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionResult;
import com.themoa.policysearch.policy.collection.service.PolicyCollectionService;
import com.themoa.policysearch.policy.domain.PolicySource;
import com.themoa.policysearch.policy.rag.index.PolicyEmbeddingSyncService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/policies")
public class PolicyAdminController {
    private final AdminKeyValidator adminKeyValidator;
    private final PolicyCollectionService collectionService;
    private final PolicyEmbeddingSyncService embeddingSyncService;

    public PolicyAdminController(AdminKeyValidator adminKeyValidator, PolicyCollectionService collectionService,
                                 PolicyEmbeddingSyncService embeddingSyncService) {
        this.adminKeyValidator = adminKeyValidator;
        this.collectionService = collectionService;
        this.embeddingSyncService = embeddingSyncService;
    }

    @PostMapping("/collect")
    public ApiResponse<List<PolicyCollectionResult>> collectAll(@RequestHeader("X-Admin-Key") String key) {
        requireAdmin(key);
        return ApiResponse.ok(collectionService.collectAll("MANUAL"));
    }

    @PostMapping("/collect/{source}")
    public ApiResponse<PolicyCollectionResult> collectSource(@RequestHeader("X-Admin-Key") String key, @PathVariable PolicySource source) {
        requireAdmin(key);
        return ApiResponse.ok(collectionService.collect(source, "MANUAL"));
    }

    @PostMapping("/retry-failed")
    public ApiResponse<Integer> retryFailed(@RequestHeader("X-Admin-Key") String key) {
        requireAdmin(key);
        return ApiResponse.ok(embeddingSyncService.retryFailed());
    }

    @PostMapping("/reindex")
    public ApiResponse<Integer> reindex(@RequestHeader("X-Admin-Key") String key) {
        requireAdmin(key);
        return ApiResponse.ok(embeddingSyncService.enqueueAll());
    }

    @PostMapping("/embedding/process-pending")
    public ApiResponse<Integer> processPending(@RequestHeader("X-Admin-Key") String key) {
        requireAdmin(key);
        return ApiResponse.ok(embeddingSyncService.processPending());
    }

    @PostMapping("/embedding/retry-failed")
    public ApiResponse<Integer> retryFailedEmbedding(@RequestHeader("X-Admin-Key") String key) {
        requireAdmin(key);
        return ApiResponse.ok(embeddingSyncService.retryFailed());
    }

    private void requireAdmin(String key) {
        if (!adminKeyValidator.isValid(key)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "관리자 인증이 필요합니다.");
        }
    }
}
