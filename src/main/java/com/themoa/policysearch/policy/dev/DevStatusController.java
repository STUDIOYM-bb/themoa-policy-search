package com.themoa.policysearch.policy.dev;

import com.themoa.policysearch.common.response.ApiResponse;
import com.themoa.policysearch.common.security.AdminKeyValidator;
import com.themoa.policysearch.policy.collection.dto.PolicySourceProbeResult;
import com.themoa.policysearch.policy.collection.service.PolicyCollectionService;
import com.themoa.policysearch.policy.collection.service.PolicyCollectionService.RegionRepairResult;
import com.themoa.policysearch.policy.domain.PolicySource;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import com.themoa.policysearch.policy.search.retrieval.PolicyRetrievalCandidate;
import com.themoa.policysearch.policy.search.retrieval.PolicyRetrievalResult;
import com.themoa.policysearch.policy.search.retrieval.QdrantPolicyRetrievalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@Profile("local")
@RequestMapping("/api/dev")
public class DevStatusController {
    private final DevStatusService devStatusService;
    private final QdrantPolicyRetrievalService qdrantRetrievalService;
    private final PolicyRepository policyRepository;
    private final PolicyCollectionService collectionService;
    private final AdminKeyValidator adminKeyValidator;

    public DevStatusController(DevStatusService devStatusService,
                               QdrantPolicyRetrievalService qdrantRetrievalService,
                               PolicyRepository policyRepository,
                               PolicyCollectionService collectionService,
                               AdminKeyValidator adminKeyValidator) {
        this.devStatusService = devStatusService;
        this.qdrantRetrievalService = qdrantRetrievalService;
        this.policyRepository = policyRepository;
        this.collectionService = collectionService;
        this.adminKeyValidator = adminKeyValidator;
    }

    @GetMapping("/status")
    public ApiResponse<DevStatusService.DevStatusResponse> status() {
        return ApiResponse.ok(devStatusService.status());
    }

    @PostMapping("/vector-search")
    public ApiResponse<List<VectorSearchItem>> vectorSearch(@Valid @RequestBody VectorSearchRequest request) {
        try {
            PolicyRetrievalResult result = qdrantRetrievalService.retrieve(request.query(), request.topK(), 0.0d);
            List<Integer> ids = result.candidates().stream().map(PolicyRetrievalCandidate::policyId).toList();
            var policies = policyRepository.findAllDetailedByIdIn(ids).stream()
                    .collect(java.util.stream.Collectors.toMap(policy -> policy.getId(), policy -> policy));
            return ApiResponse.ok(result.candidates().stream()
                    .map(candidate -> {
                        var policy = policies.get(candidate.policyId());
                        return new VectorSearchItem(candidate.documentId(), candidate.policyId(),
                                policy == null ? null : policy.getTitle(), candidate.semanticScore(),
                                policy == null ? null : policy.getSourceType().name(),
                                policy == null ? null : policy.getCategory().name(),
                                policy == null ? List.<String>of() : policy.getRegions().stream().map(pr -> pr.getRegion().displayName()).toList(),
                                candidate.metadata(), candidate.contentSnippet());
                    })
                    .toList());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Qdrant 벡터 검색 실패: " + safeMessage(ex));
        }
    }

    @GetMapping("/collection-runs")
    public ApiResponse<List<DevStatusService.CollectionRunItem>> collectionRuns(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(devStatusService.recentRuns(limit));
    }

    @GetMapping("/embedding-sync-summary")
    public ApiResponse<DevStatusService.EmbeddingSummary> embeddingSummary() {
        return ApiResponse.ok(devStatusService.embeddingSummary());
    }

    @PostMapping("/policy-sources/{source}/probe")
    public ApiResponse<PolicySourceProbeResult> probeSource(@RequestHeader("X-Admin-Key") String key,
                                                            @PathVariable PolicySource source) {
        requireAdmin(key);
        return ApiResponse.ok(collectionService.probe(source));
    }

    @PostMapping("/repair/central-source-regions")
    public ApiResponse<RegionRepairResult> repairCentralSourceRegions(@RequestHeader("X-Admin-Key") String key) {
        requireAdmin(key);
        return ApiResponse.ok(collectionService.repairCentralSourceRegions());
    }

    private void requireAdmin(String key) {
        if (!adminKeyValidator.isValid(key)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "관리자 인증이 필요합니다.");
        }
    }

    private String safeMessage(RuntimeException ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    public record VectorSearchRequest(@NotBlank String query, @Min(1) @Max(50) int topK) {
    }

    public record VectorSearchItem(String documentId, Integer policyId, String policyName, Double score,
                                   String source, String category, List<String> regionNames,
                                   java.util.Map<String, Object> metadata, String contentSnippet) {
    }
}
