package com.themoa.policysearch.policy.collection.service;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.common.config.PolicyCollectionProperties.SourceProperties;
import com.themoa.policysearch.policy.collection.client.PolicyApiParseException;
import com.themoa.policysearch.policy.collection.client.PolicyApiResponseException;
import com.themoa.policysearch.policy.collection.collector.PolicyCollector;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionResult;
import com.themoa.policysearch.policy.collection.dto.PolicySourceProbeResult;
import com.themoa.policysearch.policy.domain.*;
import com.themoa.policysearch.policy.repository.*;
import com.themoa.policysearch.policy.search.evaluator.PolicyApplicationStatusCalculator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PolicyCollectionService {
    private static final List<PolicySource> COLLECTION_ORDER = List.of(
            PolicySource.YOUTH_CENTER,
            PolicySource.GOV_SERVICE,
            PolicySource.LOCAL_WELFARE,
            PolicySource.CENTRAL_WELFARE
    );

    private final Map<PolicySource, PolicyCollector> collectors;
    private final PolicyCollectionProperties properties;
    private final PolicyRepository policyRepository;
    private final RegionCodeRepository regionCodeRepository;
    private final PolicyRawDataRepository rawDataRepository;
    private final PolicyCollectionRunRepository runRepository;
    private final PolicyCollectionErrorRepository errorRepository;
    private final PolicyEmbeddingSyncRepository embeddingSyncRepository;
    private final PolicyDeduplicationService deduplicationService;
    private final PolicyApplicationStatusCalculator statusCalculator;
    private final TransactionTemplate transactionTemplate;

    public PolicyCollectionService(List<PolicyCollector> collectors, PolicyCollectionProperties properties,
                                   PolicyRepository policyRepository, RegionCodeRepository regionCodeRepository,
                                   PolicyRawDataRepository rawDataRepository, PolicyCollectionRunRepository runRepository,
                                   PolicyCollectionErrorRepository errorRepository,
                                   PolicyEmbeddingSyncRepository embeddingSyncRepository,
                                   PolicyDeduplicationService deduplicationService,
                                   PolicyApplicationStatusCalculator statusCalculator,
                                   TransactionTemplate transactionTemplate) {
        this.collectors = collectors.stream().collect(Collectors.toMap(PolicyCollector::source, Function.identity()));
        this.properties = properties;
        this.policyRepository = policyRepository;
        this.regionCodeRepository = regionCodeRepository;
        this.rawDataRepository = rawDataRepository;
        this.runRepository = runRepository;
        this.errorRepository = errorRepository;
        this.embeddingSyncRepository = embeddingSyncRepository;
        this.deduplicationService = deduplicationService;
        this.statusCalculator = statusCalculator;
        this.transactionTemplate = transactionTemplate;
    }

    public List<PolicyCollectionResult> collectAll(String executionType) {
        return COLLECTION_ORDER.stream()
                .map(source -> isSourceEnabled(source)
                        ? collect(source, executionType)
                        : PolicyCollectionResult.skipped(source, "DISABLED"))
                .toList();
    }

    public PolicyCollectionResult collect(PolicySource source, String executionType) {
        PolicyCollector collector = collectors.get(source);
        if (collector == null) {
            throw new IllegalArgumentException("지원하지 않는 정책 출처입니다. " + source);
        }
        PolicyCollectionRun run = runRepository.save(new PolicyCollectionRun(source, executionType));
        int page = 1;
        int pages = 0;
        int requests = 0;
        int listRequests = 0;
        int detailRequests = 0;
        int supportRequests = 0;
        int failedRequests = 0;
        int received = 0;
        int processed = 0;
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        int itemFailed = 0;
        int pageFailed = 0;
        String representativeError = null;
        Set<String> seenFirstPolicyIds = new LinkedHashSet<>();
        Set<String> seenResponseHashes = new LinkedHashSet<>();
        try {
            boolean hasNext;
            do {
                try {
                    PolicyCollectionPage collectionPage = collector.collectPage(page, properties.getPageSize());
                    pages++;
                    requests += collectionPage.apiRequestCount();
                    listRequests += collectionPage.listRequestCount();
                    detailRequests += collectionPage.detailRequestCount();
                    supportRequests += collectionPage.supportConditionRequestCount();
                    failedRequests += collectionPage.failedRequestCount();
                    received += collectionPage.items().size();
                    String firstPolicyId = collectionPage.items().isEmpty() ? null : collectionPage.items().get(0).sourcePolicyId();
                    String responseHash = sha256(collectionPage.rawBody());
                    if (isRepeatedPage(firstPolicyId, responseHash, seenFirstPolicyIds, seenResponseHashes)) {
                        pageFailed++;
                        failed++;
                        representativeError = "페이지가 증가해도 동일한 응답이 반복되어 수집을 중단했습니다.";
                        hasNext = false;
                    } else if (pages > properties.getMaxPages()) {
                        pageFailed++;
                        failed++;
                        representativeError = "수집 max-pages 안전 제한에 도달했습니다.";
                        hasNext = false;
                    } else {
                    for (PolicyCollectionItem item : collectionPage.items()) {
                        try {
                            SaveOutcome outcome = saveItem(item, collectionPage.rawBody(), collectionPage.requestUrl());
                            processed++;
                            inserted += outcome.inserted ? 1 : 0;
                            updated += outcome.updated ? 1 : 0;
                            skipped += outcome.skipped ? 1 : 0;
                        } catch (RuntimeException itemEx) {
                            itemFailed++;
                            failed++;
                            representativeError = safeMessage(itemEx);
                            errorRepository.save(new PolicyCollectionError(run, source, page, item.sourcePolicyId(),
                                    itemEx.getClass().getSimpleName(), truncate(representativeError)));
                        }
                    }
                    hasNext = collectionPage.hasNext();
                    page++;
                    sleepDelay();
                    }
                } catch (RuntimeException ex) {
                    pageFailed++;
                    failed++;
                    representativeError = safeMessage(ex);
                    saveFailedRawData(source, page, ex);
                    errorRepository.save(new PolicyCollectionError(run, source, page, null,
                            ex.getClass().getSimpleName(), truncate(representativeError)));
                    hasNext = false;
                }
            } while (hasNext);
            CollectionStatus status = failed == 0
                    ? CollectionStatus.SUCCESS
                    : (inserted + updated > 0 ? CollectionStatus.PARTIAL_SUCCESS : CollectionStatus.FAILED);
            run.addStats(pages, requests, received, inserted, updated, skipped, failed);
            run.complete(status, truncate(representativeError));
            runRepository.save(run);
            return new PolicyCollectionResult(source, status.name(), pages, requests, listRequests, detailRequests,
                    supportRequests, failedRequests, received, processed, inserted, updated, skipped, failed,
                    itemFailed, pageFailed, representativeError);
        } catch (RuntimeException ex) {
            run.complete(CollectionStatus.FAILED, truncate(safeMessage(ex)));
            runRepository.save(run);
            throw ex;
        }
    }

    public PolicyCollectionResult collectFirstPage(PolicySource source, String executionType) {
        PolicyCollector collector = collectors.get(source);
        if (collector == null) {
            throw new IllegalArgumentException("지원하지 않는 정책 출처입니다. " + source);
        }
        PolicyCollectionRun run = runRepository.save(new PolicyCollectionRun(source, executionType));
        int requests = 0;
        int received = 0;
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        String representativeError = null;
        try {
            try {
                PolicyCollectionPage collectionPage = collector.collectPage(1, Math.min(properties.getPageSize(), 10));
                requests += collectionPage.apiRequestCount();
                received += collectionPage.items().size();
                for (PolicyCollectionItem item : collectionPage.items()) {
                    SaveOutcome outcome = saveItem(item, collectionPage.rawBody(), collectionPage.requestUrl());
                    inserted += outcome.inserted ? 1 : 0;
                    updated += outcome.updated ? 1 : 0;
                    skipped += outcome.skipped ? 1 : 0;
                }
            } catch (RuntimeException ex) {
                failed++;
                representativeError = safeMessage(ex);
                saveFailedRawData(source, 1, ex);
                errorRepository.save(new PolicyCollectionError(run, source, 1, null,
                        ex.getClass().getSimpleName(), truncate(representativeError)));
            }
            CollectionStatus status = failed == 0
                    ? CollectionStatus.SUCCESS
                    : (inserted + updated > 0 ? CollectionStatus.PARTIAL_SUCCESS : CollectionStatus.FAILED);
            run.addStats(failed == 0 ? 1 : 0, requests, received, inserted, updated, skipped, failed);
            run.complete(status, truncate(representativeError));
            runRepository.save(run);
            return new PolicyCollectionResult(source, failed == 0 ? 1 : 0, requests, received, inserted, updated,
                    skipped, failed, representativeError);
        } catch (RuntimeException ex) {
            run.complete(CollectionStatus.FAILED, truncate(safeMessage(ex)));
            runRepository.save(run);
            throw ex;
        }
    }

    public PolicySourceProbeResult probe(PolicySource source) {
        PolicyCollector collector = collectors.get(source);
        if (collector == null) {
            throw new IllegalArgumentException("지원하지 않는 정책 출처입니다. " + source);
        }
        try {
            PolicyCollectionPage page = collector.collectPage(1, Math.min(properties.getPageSize(), 10));
            return new PolicySourceProbeResult(source, keyConfigured(source), true, page.requestUrl(), page.statusCode(),
                    page.responseContentType(), length(page.rawBody()), responseType(page.responseContentType(), page.rawBody()), preview(page.rawBody()),
                    false, null, null, true, page.totalCount(), page.items().size(),
                    page.items().isEmpty() ? null : page.items().get(0).sourcePolicyId(),
                    page.items().isEmpty() ? null : page.items().get(0).policyName());
        } catch (PolicyApiResponseException ex) {
            return new PolicySourceProbeResult(source, keyConfigured(source), true, ex.maskedRequestUrl(),
                    ex.statusCode(), ex.contentType(), length(ex.responseBody()), responseType(ex.contentType(), ex.responseBody()), preview(ex.responseBody()),
                    true, ex.apiErrorCode(), ex.apiErrorMessage(), false, null, 0, null, null);
        } catch (PolicyApiParseException ex) {
            return new PolicySourceProbeResult(source, keyConfigured(source), true, ex.maskedRequestUrl(),
                    null, ex.responseContentType(), length(ex.rawBody()), responseType(ex.responseContentType(), ex.rawBody()), ex.responsePreview(),
                    false, null, ex.getMessage(), false, null, 0, null, null);
        } catch (RuntimeException ex) {
            return new PolicySourceProbeResult(source, keyConfigured(source), false, null, null,
                    null, 0, null, "", true, ex.getClass().getSimpleName(), safeMessage(ex),
                    false, null, 0, null, null);
        }
    }

    @Transactional
    public RegionRepairResult repairCentralSourceRegions() {
        List<Policy> policies = policyRepository.findAllBySourceTypeIn(List.of(PolicySource.GOV_SERVICE, PolicySource.CENTRAL_WELFARE));
        int changed = 0;
        for (Policy policy : policies) {
            List<String> before = policy.getRegions().stream().map(policyRegion -> policyRegion.getRegion().displayName()).toList();
            syncRegions(policy, List.of("전국"));
            List<String> after = policy.getRegions().stream().map(policyRegion -> policyRegion.getRegion().displayName()).toList();
            if (!before.equals(after)) {
                changed++;
                embeddingSyncRepository.findByPolicyId(policy.getId())
                        .orElseGet(() -> embeddingSyncRepository.save(new PolicyEmbeddingSync(policy)))
                        .markPending(null);
            }
        }
        return new RegionRepairResult(policies.size(), changed);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SaveOutcome saveItem(PolicyCollectionItem item, String rawBody, String requestUrl) {
        return transactionTemplate.execute(status -> saveItemInTransaction(item, rawBody, requestUrl));
    }

    private SaveOutcome saveItemInTransaction(PolicyCollectionItem item, String rawBody, String requestUrl) {
        rawDataRepository.save(new PolicyRawData(item.source(), item.sourcePolicyId(), requestUrl, "{}",
                maskSecret(rawBody), "AUTO", "SUCCESS", null));
        if (item.policyName() == null || item.policyName().isBlank()) {
            return SaveOutcome.skippedOutcome();
        }
        Policy policy = deduplicationService.findSameSourceDuplicate(item)
                .orElseGet(() -> new Policy(item.policyName(), item.sourcePolicyId(), item.source(), item.organization(), item.category()));
        boolean inserted = policy.getId() == null;
        policy.updateBasic(item.policyName(), defaultText(item.organization(), "확인 필요"), item.category(),
                compactSummary(item), item.officialUrl(), item.applicationStartDate(), item.applicationEndDate(),
                item.alwaysOpen(), item.alwaysOpen() ? ApplicationStatus.ALWAYS_OPEN : ApplicationStatus.NEEDS_CONFIRMATION);
        policy.replaceCondition(new PolicyCondition(item.minimumAge(), item.maximumAge(), truncateTo(item.employmentConditions(), 50),
                item.studentConditions(), truncateTo(item.incomeCondition(), 200), truncateTo(item.selectionCriteria(), 500), true));
        Policy saved = policyRepository.save(policy);
        syncRegions(saved, item.regionNames());
        ApplicationStatus calculated = statusCalculator.calculate(saved);
        saved.updateBasic(saved.getTitle(), saved.getAgencyName(), saved.getCategory(), saved.getSummary(), saved.getOfficialUrl(),
                saved.getStartDate(), saved.getDueDate(), saved.isAlwaysOpen(), calculated);
        policyRepository.save(saved);
        embeddingSyncRepository.findByPolicyId(saved.getId())
                .orElseGet(() -> embeddingSyncRepository.save(new PolicyEmbeddingSync(saved)))
                .markPending(null);
        return inserted ? SaveOutcome.insertedOutcome() : SaveOutcome.updatedOutcome();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long saveFailedRawData(PolicySource source, int page, RuntimeException ex) {
        if (ex instanceof PolicyApiParseException parseException) {
            PolicyRawData raw = rawDataRepository.save(new PolicyRawData(source, null, parseException.maskedRequestUrl(),
                    "{\"page\":" + page + "}", maskSecret(parseException.rawBody()), parseException.parserType(),
                    "PARSE_FAILED", truncate(maskSecret(parseException.getMessage()))));
            return raw.getId();
        }
        if (ex instanceof PolicyApiResponseException responseException) {
            PolicyRawData raw = rawDataRepository.save(new PolicyRawData(source, null, responseException.maskedRequestUrl(),
                    "{\"page\":" + page + "}", maskSecret(responseException.responseBody()), "ERROR",
                    "API_ERROR", truncate(maskSecret(responseException.getMessage()))));
            return raw.getId();
        }
        return null;
    }

    private RegionCode findOrCreateRegion(String regionName) {
        String code = regionCode(regionName);
        return regionCodeRepository.findByRegionCode(code)
                .orElseGet(() -> regionCodeRepository.save(new RegionCode(code, province(regionName), city(regionName), level(regionName))));
    }

    private void syncRegions(Policy policy, List<String> regionNames) {
        Set<String> desiredNames = regionNames == null ? Set.of() : regionNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, RegionCode> desiredRegions = desiredNames.stream()
                .map(this::findOrCreateRegion)
                .collect(Collectors.toMap(RegionCode::getRegionCode, Function.identity(), (left, right) -> left));
        policy.getRegions().removeIf(policyRegion ->
                !desiredRegions.containsKey(policyRegion.getRegion().getRegionCode()));
        Set<String> existingCodes = policy.getRegions().stream()
                .map(policyRegion -> policyRegion.getRegion().getRegionCode())
                .collect(Collectors.toSet());
        for (RegionCode region : desiredRegions.values()) {
            if (!existingCodes.contains(region.getRegionCode())) {
                policy.getRegions().add(new PolicyRegion(policy, region));
            }
        }
    }

    private String regionCode(String regionName) {
        String normalized = regionName.replaceAll("[^가-힣A-Za-z0-9_]", "_").replaceAll("_+", "_");
        String hash = Integer.toHexString(regionName.hashCode());
        String prefix = normalized.length() > 20 ? normalized.substring(0, 20) : normalized;
        return prefix + "_" + hash;
    }

    private String province(String regionName) {
        if ("전국".equals(regionName)) return "전국";
        int idx = regionName.indexOf(' ');
        return idx > 0 ? regionName.substring(0, idx) : regionName;
    }

    private String city(String regionName) {
        int idx = regionName.indexOf(' ');
        return idx > 0 ? regionName.substring(idx + 1) : null;
    }

    private String level(String regionName) {
        if ("전국".equals(regionName)) return "COUNTRY";
        return regionName.contains(" ") ? "CITY" : "PROVINCE";
    }

    private String compactSummary(PolicyCollectionItem item) {
        return defaultText(item.supportContent(), item.selectionCriteria(), item.applicationMethod(), "내용 확인 필요");
    }

    private String defaultText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.length() > 500 ? value.substring(0, 500) : value;
            }
        }
        return "";
    }

    private boolean keyConfigured(PolicySource source) {
        return switch (source) {
            case YOUTH_CENTER -> hasKey(properties.getYouthCenter());
            case GOV_SERVICE -> hasKey(properties.getGovService());
            case LOCAL_WELFARE -> hasKey(properties.getLocalWelfare());
            case CENTRAL_WELFARE -> hasKey(properties.getCentralWelfare());
        };
    }

    private boolean hasKey(SourceProperties props) {
        return props.getApiKey() != null && !props.getApiKey().isBlank();
    }

    private int length(String value) {
        return value == null ? 0 : value.length();
    }

    private String preview(String value) {
        String masked = maskSecret(value);
        if (masked == null) {
            return "";
        }
        return masked.length() > 1000 ? masked.substring(0, 1000) : masked;
    }

    private String responseType(String contentType, String body) {
        String trimmed = body == null ? "" : body.stripLeading().toLowerCase();
        if (trimmed.startsWith("<!doctype html") || trimmed.startsWith("<html")) {
            return "HTML";
        }
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return "JSON";
        }
        if (trimmed.startsWith("<")) {
            return "XML";
        }
        String ct = contentType == null ? "" : contentType.toLowerCase();
        if (ct.contains("json")) {
            return "JSON";
        }
        if (ct.contains("xml")) {
            return "XML";
        }
        return null;
    }

    private boolean isSourceEnabled(PolicySource source) {
        return switch (source) {
            case YOUTH_CENTER -> properties.getSources().isYouthCenterEnabled();
            case GOV_SERVICE -> properties.getSources().isGovServiceEnabled();
            case LOCAL_WELFARE -> properties.getSources().isLocalWelfareEnabled();
            case CENTRAL_WELFARE -> properties.getSources().isCentralWelfareEnabled();
        };
    }

    private boolean isRepeatedPage(String firstPolicyId, String responseHash, Set<String> seenFirstPolicyIds,
                                   Set<String> seenResponseHashes) {
        boolean repeatedFirstId = firstPolicyId != null && !firstPolicyId.isBlank() && !seenFirstPolicyIds.add(firstPolicyId);
        boolean repeatedHash = responseHash != null && !responseHash.isBlank() && !seenResponseHashes.add(responseHash);
        return repeatedFirstId || repeatedHash;
    }

    private String sha256(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex);
        }
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return maskSecret(message == null || message.isBlank() ? ex.getClass().getSimpleName() : message);
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private String truncateTo(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private String maskSecret(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("(?i)(serviceKey|openApiVlak|apiKey)=([^&\\s<]+)", "$1=****");
    }

    private void sleepDelay() {
        try {
            Thread.sleep(properties.getRequestDelay().toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("수집 대기가 중단되었습니다.", ex);
        }
    }

    public record SaveOutcome(boolean inserted, boolean updated, boolean skipped) {
        static SaveOutcome insertedOutcome() { return new SaveOutcome(true, false, false); }
        static SaveOutcome updatedOutcome() { return new SaveOutcome(false, true, false); }
        static SaveOutcome skippedOutcome() { return new SaveOutcome(false, false, true); }
    }

    public record RegionRepairResult(int checkedPolicyCount, int changedPolicyCount) {
    }
}
