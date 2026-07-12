package com.themoa.policysearch.policy.search.service;

import com.themoa.policysearch.common.exception.NotFoundException;
import com.themoa.policysearch.policy.domain.ApplicationStatus;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.repository.PolicyBookmarkRepository;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import com.themoa.policysearch.policy.search.dto.PolicyDetailResponse;
import com.themoa.policysearch.policy.search.dto.PolicyResultItem;
import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;
import com.themoa.policysearch.policy.search.dto.PolicySearchRequest;
import com.themoa.policysearch.policy.search.dto.PolicySearchResponse;
import com.themoa.policysearch.policy.search.evaluator.PolicyApplicationStatusCalculator;
import com.themoa.policysearch.policy.search.evaluator.PolicyEligibilityEvaluator;
import com.themoa.policysearch.policy.search.parser.PolicyQueryParseResult;
import com.themoa.policysearch.policy.search.parser.PolicyQueryParser;
import com.themoa.policysearch.policy.search.retrieval.MysqlFallbackPolicyRetrievalService;
import com.themoa.policysearch.policy.search.retrieval.PolicyRetrievalCandidate;
import com.themoa.policysearch.policy.search.retrieval.PolicyRetrievalResult;
import com.themoa.policysearch.policy.search.retrieval.QdrantPolicyRetrievalService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicySearchService {
    private static final int DEFAULT_VECTOR_TOP_K = 30;
    private static final int FALLBACK_LIMIT = 100;

    private final PolicyQueryParser parser;
    private final QdrantPolicyRetrievalService qdrantRetrievalService;
    private final MysqlFallbackPolicyRetrievalService mysqlFallbackPolicyRetrievalService;
    private final PolicyRepository policyRepository;
    private final PolicyBookmarkRepository bookmarkRepository;
    private final PolicyEligibilityEvaluator eligibilityEvaluator;
    private final PolicyApplicationStatusCalculator applicationStatusCalculator;
    private final boolean ragEnabled;

    public PolicySearchService(PolicyQueryParser parser,
                               QdrantPolicyRetrievalService qdrantRetrievalService,
                               MysqlFallbackPolicyRetrievalService mysqlFallbackPolicyRetrievalService,
                               PolicyRepository policyRepository,
                               PolicyBookmarkRepository bookmarkRepository,
                               PolicyEligibilityEvaluator eligibilityEvaluator,
                               PolicyApplicationStatusCalculator applicationStatusCalculator,
                               @Value("${app.rag.enabled:false}") boolean ragEnabled) {
        this.parser = parser;
        this.qdrantRetrievalService = qdrantRetrievalService;
        this.mysqlFallbackPolicyRetrievalService = mysqlFallbackPolicyRetrievalService;
        this.policyRepository = policyRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.eligibilityEvaluator = eligibilityEvaluator;
        this.applicationStatusCalculator = applicationStatusCalculator;
        this.ragEnabled = ragEnabled;
    }

    @Transactional(readOnly = true)
    public PolicySearchResponse search(PolicySearchRequest request, Integer memberId) {
        Instant startedAt = Instant.now();
        PolicyQueryParseResult parseResult = parser.parseQuery(request.query());
        PolicySearchCondition condition = parseResult.condition();
        applySupplemental(condition, request.supplementalConditions());

        List<String> globalMissing = globalMissing(condition);
        boolean needsMore = globalMissing.size() >= 4;
        if (needsMore) {
            return new PolicySearchResponse(request.query(), condition, globalMissing, true,
                    followUps(globalMissing), parseResult.parserMode().name(), parseResult.fallback(),
                    parseResult.fallbackReason(), "NEEDS_MORE_INFORMATION", false, false, true,
                    "검색 조건이 부족합니다.", 0, 0, 0, elapsedMillis(startedAt),
                    "검색을 시작하기 전에 몇 가지 조건을 더 입력해주세요.", List.of(),
                    request.page(), request.size(), 0);
        }

        PolicyRetrievalResult retrieval = retrieve(request, condition);
        Map<Integer, PolicyRetrievalCandidate> candidateMap = retrieval.candidates().stream()
                .collect(Collectors.toMap(PolicyRetrievalCandidate::policyId, Function.identity(),
                        (left, right) -> left, LinkedHashMap::new));
        List<Integer> orderedIds = new ArrayList<>(candidateMap.keySet());
        List<Policy> policies = orderedIds.isEmpty()
                ? List.of()
                : orderByCandidate(policyRepository.findAllDetailedByIdIn(orderedIds), orderedIds);

        List<PolicyResultItem> filtered = new ArrayList<>();
        for (Policy policy : policies) {
            PolicyEligibilityEvaluator.Evaluation evaluation = eligibilityEvaluator.evaluate(policy, condition);
            if (!policy.isActive() || evaluation.unmatchedConditions().contains("region")
                    || targetClearlyMismatched(policy, condition, evaluation)) {
                continue;
            }
            PolicyRetrievalCandidate candidate = candidateMap.get(policy.getId());
            filtered.add(toResult(policy, condition, evaluation, candidate == null ? null : candidate.semanticScore(), memberId));
        }

        List<PolicyResultItem> sorted = filtered.stream()
                .sorted(resultComparator())
                .toList();
        int from = Math.min(request.page() * request.size(), sorted.size());
        int to = Math.min(from + request.size(), sorted.size());
        List<PolicyResultItem> pageResults = sorted.subList(from, to);
        String message = pageResults.isEmpty()
                ? "현재 입력하신 지역과 조건에 맞는 정책을 찾지 못했습니다."
                : "입력하신 조건을 기준으로 정책을 찾았습니다. 최종 자격 여부는 공식 기관 확인이 필요합니다.";

        return new PolicySearchResponse(request.query(), condition, globalMissing, false,
                followUps(globalMissing), parseResult.parserMode().name(), parseResult.fallback(),
                parseResult.fallbackReason(), retrieval.searchMode().name(), retrieval.ragAttempted(),
                retrieval.ragSucceeded(), retrieval.fallback(), retrieval.fallbackReason(),
                retrieval.vectorCandidateCount(), policies.size(), filtered.size(), elapsedMillis(startedAt),
                message, pageResults, request.page(), request.size(), filtered.size());
    }

    @Transactional(readOnly = true)
    public PolicyDetailResponse detail(Integer policyId) {
        Policy policy = policyRepository.findAllDetailedByIdIn(List.of(policyId)).stream().findFirst()
                .orElseThrow(() -> new NotFoundException("정책을 찾을 수 없습니다."));
        return new PolicyDetailResponse(policy.getId(), policy.getTitle(), policy.getAgencyName(),
                List.of(policy.getSourceType().name()), regionNames(policy), policy.getCategory().name(),
                valueOrDefault(policy.getSummary()), conditionSummary(policy),
                applicationPeriod(policy), applicationStatusCalculator.calculate(policy).name(),
                "정보가 제공되지 않았습니다.", "정보가 제공되지 않았습니다.", "정보가 제공되지 않았습니다.",
                safeUrl(policy.getOfficialUrl()), "최종 자격 여부는 공식 기관 확인이 필요합니다.");
    }

    private PolicyRetrievalResult retrieve(PolicySearchRequest request, PolicySearchCondition condition) {
        int topK = Math.max(DEFAULT_VECTOR_TOP_K, request.size() * 3);
        if (ragEnabled) {
            try {
                PolicyRetrievalResult ragResult = qdrantRetrievalService.retrieve(request.query(), topK);
                if (!ragResult.candidates().isEmpty()) {
                    return ragResult;
                }
                return mysqlFallbackPolicyRetrievalService.retrieve(request.query(), condition, FALLBACK_LIMIT,
                        "Qdrant 검색 후보가 없어 MySQL 검색을 사용했습니다.")
                        .withRagAttempt(false, ragResult.vectorCandidateCount());
            } catch (RuntimeException ex) {
                return mysqlFallbackPolicyRetrievalService.retrieve(request.query(), condition, FALLBACK_LIMIT,
                        "Qdrant 검색 실패: " + safeMessage(ex))
                        .withRagAttempt(false, 0);
            }
        }
        return mysqlFallbackPolicyRetrievalService.retrieve(request.query(), condition, FALLBACK_LIMIT,
                "RAG가 비활성화되어 MySQL 검색을 사용했습니다.");
    }

    private PolicyResultItem toResult(Policy policy, PolicySearchCondition condition,
                                      PolicyEligibilityEvaluator.Evaluation evaluation,
                                      Double semanticScore, Integer memberId) {
        ApplicationStatus applicationStatus = applicationStatusCalculator.calculate(policy);
        boolean bookmarked = memberId != null && bookmarkRepository.existsByMemberIdAndPolicyId(memberId, policy.getId());
        return new PolicyResultItem(policy.getId(), policy.getTitle(), List.of(policy.getAgencyName()),
                List.of(policy.getSourceType().name()), regionNames(policy), policy.getCategory().name(),
                conditionSummary(policy), valueOrDefault(policy.getSummary()), applicationPeriod(policy),
                applicationStatus, evaluation.status(), evaluation.matchedConditions(), evaluation.missingConditions(),
                evaluation.unmatchedConditions(), recommendationReason(evaluation), safeUrl(policy.getOfficialUrl()),
                semanticScore, bookmarked);
    }

    private List<Policy> orderByCandidate(List<Policy> policies, List<Integer> orderedIds) {
        Map<Integer, Integer> order = new LinkedHashMap<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            order.put(orderedIds.get(i), i);
        }
        return policies.stream()
                .sorted(Comparator.comparingInt(policy -> order.getOrDefault(policy.getId(), Integer.MAX_VALUE)))
                .toList();
    }

    private Comparator<PolicyResultItem> resultComparator() {
        return Comparator
                .comparing((PolicyResultItem item) -> item.eligibilityStatus().ordinal())
                .thenComparing(item -> item.semanticScore() == null ? 1.0d : -item.semanticScore());
    }

    private boolean targetClearlyMismatched(Policy policy, PolicySearchCondition condition,
                                            PolicyEligibilityEvaluator.Evaluation evaluation) {
        if (evaluation.unmatchedConditions().contains("age")) {
            return true;
        }
        String targetText = (conditionSummary(policy) + " " + policy.getTitle()).toLowerCase();
        boolean userLooksForYouth = condition.getAgeGroup() != null && condition.getAgeGroup().contains("청년")
                || condition.getTargetGroups().stream().anyMatch(value -> value.contains("청년"));
        if (!userLooksForYouth) {
            return false;
        }
        return containsAny(targetText, "노인", "고령", "장애", "노숙", "아동", "영유아");
    }

    private List<String> regionNames(Policy policy) {
        return policy.getRegions().stream().map(pr -> pr.getRegion().displayName()).toList();
    }

    private String applicationPeriod(Policy policy) {
        if (policy.isAlwaysOpen()) return "상시 신청";
        if (policy.getStartDate() != null && policy.getDueDate() != null) return policy.getStartDate() + " ~ " + policy.getDueDate();
        if (policy.getStartDate() != null) return policy.getStartDate() + "부터";
        if (policy.getDueDate() != null) return policy.getDueDate() + "까지";
        return "확인 필요";
    }

    private String conditionSummary(Policy policy) {
        return policy.getCondition() == null ? "정보가 제공되지 않았습니다." : valueOrDefault(policy.getCondition().getConditionSummary());
    }

    private String recommendationReason(PolicyEligibilityEvaluator.Evaluation evaluation) {
        if (!evaluation.unmatchedConditions().isEmpty()) return "입력 조건과 맞지 않는 항목이 있어 신청이 어려울 수 있습니다.";
        if (!evaluation.missingConditions().isEmpty()) return "관련 정책이지만 추가 조건 확인이 필요합니다.";
        return "입력하신 조건과 주요 조건이 일치합니다.";
    }

    private List<String> globalMissing(PolicySearchCondition condition) {
        List<String> missing = new ArrayList<>();
        if (condition.getRegion() == null) missing.add("region");
        if (condition.getAge() == null && condition.getAgeGroup() == null) missing.add("age");
        if (condition.getEmploymentStatus() == null) missing.add("employmentStatus");
        if (condition.getStudentStatus() == null) missing.add("studentStatus");
        if (condition.getCategory() == null && condition.getKeywords().isEmpty()) missing.add("category");
        return missing;
    }

    private List<String> followUps(List<String> missing) {
        List<String> questions = new ArrayList<>();
        if (missing.contains("region")) questions.add("거주 지역이 어디인가요?");
        if (missing.contains("age")) questions.add("나이가 어떻게 되나요?");
        if (missing.contains("employmentStatus")) questions.add("현재 재직, 구직, 무직 중 어떤 상태인가요?");
        if (missing.contains("studentStatus")) questions.add("학생 여부를 알려주세요.");
        if (missing.contains("category")) questions.add("관심 분야가 무엇인가요?");
        return questions;
    }

    private void applySupplemental(PolicySearchCondition condition, Map<String, String> supplemental) {
        if (supplemental == null) return;
        if (hasText(supplemental.get("region"))) condition.setRegion(supplemental.get("region"));
        if (hasText(supplemental.get("age"))) condition.setAge(Integer.parseInt(supplemental.get("age")));
        if (hasText(supplemental.get("employmentStatus"))) condition.setEmploymentStatus(supplemental.get("employmentStatus"));
        if (hasText(supplemental.get("studentStatus"))) condition.setStudentStatus(Boolean.parseBoolean(supplemental.get("studentStatus")));
        if (hasText(supplemental.get("incomeStatus"))) condition.setIncomeCondition(supplemental.get("incomeStatus"));
        if (hasText(supplemental.get("housingStatus"))) condition.setHousingStatus(supplemental.get("housingStatus"));
        if (hasText(supplemental.get("householdStatus"))) condition.setHouseholdStatus(supplemental.get("householdStatus"));
        if (hasText(supplemental.get("category"))) condition.setCategory(supplemental.get("category"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) return true;
        }
        return false;
    }

    private String valueOrDefault(String value) {
        return value == null || value.isBlank() ? "정보가 제공되지 않았습니다." : value;
    }

    private String safeUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.startsWith("http://") || value.startsWith("https://") ? value : null;
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }

    private long elapsedMillis(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }
}
