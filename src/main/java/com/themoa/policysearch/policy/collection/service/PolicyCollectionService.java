package com.themoa.policysearch.policy.collection.service;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.collection.collector.PolicyCollector;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionResult;
import com.themoa.policysearch.policy.domain.*;
import com.themoa.policysearch.policy.repository.*;
import com.themoa.policysearch.policy.search.evaluator.PolicyApplicationStatusCalculator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyCollectionService {
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

    public PolicyCollectionService(List<PolicyCollector> collectors, PolicyCollectionProperties properties,
                                   PolicyRepository policyRepository, RegionCodeRepository regionCodeRepository,
                                   PolicyRawDataRepository rawDataRepository, PolicyCollectionRunRepository runRepository,
                                   PolicyCollectionErrorRepository errorRepository,
                                   PolicyEmbeddingSyncRepository embeddingSyncRepository,
                                   PolicyDeduplicationService deduplicationService,
                                   PolicyApplicationStatusCalculator statusCalculator) {
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
    }

    public List<PolicyCollectionResult> collectAll(String executionType) {
        return collectors.keySet().stream().map(source -> collect(source, executionType)).toList();
    }

    public PolicyCollectionResult collect(PolicySource source, String executionType) {
        PolicyCollector collector = collectors.get(source);
        if (collector == null) {
            throw new IllegalArgumentException("지원하지 않는 정책 출처입니다: " + source);
        }
        PolicyCollectionRun run = runRepository.save(new PolicyCollectionRun(source, executionType));
        int page = 1;
        int pages = 0;
        int requests = 0;
        int received = 0;
        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        String representativeError = null;
        try {
            boolean hasNext;
            do {
                try {
                    PolicyCollectionPage collectionPage = collector.collectPage(page, properties.getPageSize());
                    pages++;
                    requests += collectionPage.apiRequestCount();
                    received += collectionPage.items().size();
                    for (PolicyCollectionItem item : collectionPage.items()) {
                        SaveOutcome outcome = saveItem(item, collectionPage.rawBody(), collectionPage.requestUrl());
                        inserted += outcome.inserted ? 1 : 0;
                        updated += outcome.updated ? 1 : 0;
                        skipped += outcome.skipped ? 1 : 0;
                    }
                    hasNext = collectionPage.hasNext();
                    page++;
                    sleepDelay();
                } catch (RuntimeException ex) {
                    failed++;
                    representativeError = ex.getMessage();
                    errorRepository.save(new PolicyCollectionError(run, source, page, null, ex.getClass().getSimpleName(), ex.getMessage()));
                    hasNext = false;
                }
            } while (hasNext);
            CollectionStatus status = failed == 0 ? CollectionStatus.SUCCESS : (inserted + updated > 0 ? CollectionStatus.PARTIAL_SUCCESS : CollectionStatus.FAILED);
            run.addStats(pages, requests, received, inserted, updated, skipped, failed);
            run.complete(status, representativeError);
            runRepository.save(run);
            return new PolicyCollectionResult(pages, requests, received, inserted, updated, skipped, failed, representativeError);
        } catch (RuntimeException ex) {
            run.complete(CollectionStatus.FAILED, ex.getMessage());
            runRepository.save(run);
            throw ex;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SaveOutcome saveItem(PolicyCollectionItem item, String rawBody, String requestUrl) {
        PolicyRawData raw = rawDataRepository.save(new PolicyRawData(item.source(), item.sourcePolicyId(), requestUrl, "{}", rawBody, "AUTO", "SUCCESS", null));
        if (item.policyName() == null || item.policyName().isBlank()) {
            return SaveOutcome.skippedOutcome();
        }
        Policy policy = deduplicationService.findSameSourceDuplicate(item)
                .orElseGet(() -> new Policy(item.policyName(), item.sourcePolicyId(), item.source(), item.organization(), item.category()));
        boolean inserted = policy.getId() == null;
        policy.updateBasic(item.policyName(), defaultText(item.organization(), "확인 필요"), item.category(),
                compactSummary(item), item.officialUrl(), item.applicationStartDate(), item.applicationEndDate(),
                item.alwaysOpen(), item.alwaysOpen() ? ApplicationStatus.ALWAYS_OPEN : ApplicationStatus.NEEDS_CONFIRMATION);
        policy.replaceCondition(new PolicyCondition(item.minimumAge(), item.maximumAge(), item.employmentConditions(),
                item.studentConditions(), item.incomeCondition(), item.selectionCriteria(), true));
        policy.getRegions().clear();
        Policy saved = policyRepository.save(policy);
        for (String regionName : item.regionNames()) {
            RegionCode region = findOrCreateRegion(regionName);
            saved.getRegions().add(new PolicyRegion(saved, region));
        }
        ApplicationStatus calculated = statusCalculator.calculate(saved);
        saved.updateBasic(saved.getTitle(), saved.getAgencyName(), saved.getCategory(), saved.getSummary(), saved.getOfficialUrl(),
                saved.getStartDate(), saved.getDueDate(), saved.isAlwaysOpen(), calculated);
        policyRepository.save(saved);
        embeddingSyncRepository.findByPolicyId(saved.getId())
                .orElseGet(() -> embeddingSyncRepository.save(new PolicyEmbeddingSync(saved)))
                .markPending(null);
        return inserted ? SaveOutcome.insertedOutcome() : SaveOutcome.updatedOutcome();
    }

    private RegionCode findOrCreateRegion(String regionName) {
        String code = regionName.replace(" ", "_");
        return regionCodeRepository.findByRegionCode(code)
                .orElseGet(() -> regionCodeRepository.save(new RegionCode(code, province(regionName), city(regionName), level(regionName))));
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

    private void sleepDelay() {
        try {
            Thread.sleep(properties.getRequestDelay().toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("수집 대기 중단", ex);
        }
    }

    public record SaveOutcome(boolean inserted, boolean updated, boolean skipped) {
        static SaveOutcome insertedOutcome() { return new SaveOutcome(true, false, false); }
        static SaveOutcome updatedOutcome() { return new SaveOutcome(false, true, false); }
        static SaveOutcome skippedOutcome() { return new SaveOutcome(false, false, true); }
    }
}
