package com.themoa.policysearch.policy.collection.collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.collection.client.ExternalPolicyApiClient;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionPage;
import com.themoa.policysearch.policy.collection.normalizer.*;
import com.themoa.policysearch.policy.domain.PolicyCategory;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.time.LocalDateTime;
import java.util.*;

abstract class AbstractTreePolicyCollector implements PolicyCollector {
    protected final ExternalPolicyApiClient apiClient;
    protected final PolicyCollectionProperties properties;
    protected final RegionNormalizer regionNormalizer;
    protected final CategoryNormalizer categoryNormalizer;
    protected final DateRangeNormalizer dateRangeNormalizer;
    protected final AgeConditionNormalizer ageConditionNormalizer;
    protected final KeywordNormalizer keywordNormalizer;
    protected final ObjectMapper jsonMapper = new ObjectMapper();
    protected final XmlMapper xmlMapper = new XmlMapper();

    AbstractTreePolicyCollector(ExternalPolicyApiClient apiClient, PolicyCollectionProperties properties,
                                RegionNormalizer regionNormalizer, CategoryNormalizer categoryNormalizer,
                                DateRangeNormalizer dateRangeNormalizer, AgeConditionNormalizer ageConditionNormalizer,
                                KeywordNormalizer keywordNormalizer) {
        this.apiClient = apiClient;
        this.properties = properties;
        this.regionNormalizer = regionNormalizer;
        this.categoryNormalizer = categoryNormalizer;
        this.dateRangeNormalizer = dateRangeNormalizer;
        this.ageConditionNormalizer = ageConditionNormalizer;
        this.keywordNormalizer = keywordNormalizer;
    }

    protected PolicyCollectionPage parsePage(int page, int size, String rawBody, String requestUrl, boolean xml) {
        try {
            JsonNode root = xml ? xmlMapper.readTree(rawBody) : jsonMapper.readTree(rawBody);
            List<JsonNode> nodes = listNodes(root);
            List<PolicyCollectionItem> items = new ArrayList<>();
            for (JsonNode node : nodes) {
                PolicyCollectionItem item = toItem(node);
                if (item.policyName() != null && !item.policyName().isBlank()) {
                    items.add(item);
                }
            }
            int totalCount = intValue(root, "totalCount", "totalCnt", "totCnt");
            boolean hasNext = totalCount > 0 ? page * size < totalCount : !items.isEmpty();
            return new PolicyCollectionPage(items, page, 1, totalCount, hasNext, rawBody, requestUrl);
        } catch (Exception ex) {
            throw new IllegalArgumentException("정책 API 응답 파싱 실패", ex);
        }
    }

    protected PolicyCollectionItem commonItem(JsonNode node, PolicySource source) {
        String id = firstText(node, "plcyNo", "bizId", "servId", "서비스ID", "svcId", "id", "policyId");
        String name = firstText(node, "plcyNm", "servNm", "서비스명", "svcNm", "title", "name");
        String organization = firstText(node, "sprvsnInstCdNm", "jurMnofNm", "부서명", "agencyName", "organNm", "deptNm");
        String categoryRaw = firstText(node, "lclsfNm", "plcyKywdNm", "category", "bizTycdSel", "서비스분야");
        String regionRaw = firstText(node, "zipCd", "region", "sidoNm", "시도명", "지자체명", "areaNm");
        String target = firstText(node, "sprtTrgtCn", "서비스목적요약", "지원대상", "target", "lifeArray");
        String support = firstText(node, "plcySprtCn", "서비스내용", "지원내용", "supportContent", "summary");
        String condition = firstText(node, "aplyQlfcCn", "선정기준", "지원대상", "condition", "criteria");
        String method = firstText(node, "aplyMthdCn", "신청방법", "applicationMethod", "servSeDetailLink");
        String period = firstText(node, "aplyYmd", "신청기한", "applicationPeriod", "period");
        String url = firstText(node, "refUrlAddr1", "servSeDetailLink", "url", "officialUrl", "서비스URL");
        String contact = firstText(node, "inqryTelno", "문의처", "contact", "telno");
        DateRange dateRange = dateRangeNormalizer.normalize(period);
        AgeConditionNormalizer.AgeRange age = ageConditionNormalizer.normalize(target + " " + condition);
        PolicyCategory category = categoryNormalizer.normalize(categoryRaw + " " + name + " " + support);
        List<String> regions = regionNormalizer.normalize(regionRaw + " " + organization);
        List<String> keywords = keywordNormalizer.normalize(name, target, support, condition, categoryRaw);
        return new PolicyCollectionItem(source, id == null ? stableId(source, name, organization, regions) : id,
                name, organization == null ? "확인 필요" : organization, organization, regions, List.of(),
                category, target == null ? List.of() : List.of(target), age.minimumAge(), age.maximumAge(),
                conditionText(condition, "취업"), condition != null && condition.contains("학생") ? true : null,
                conditionText(condition, "소득"), conditionText(condition, "주거"), conditionText(condition, "가구"),
                condition, support, method, firstText(node, "제출서류", "requiredDocuments"),
                dateRange.startDate(), dateRange.endDate(), dateRange.rawText(), dateRange.alwaysOpen(),
                url, contact, keywords, null, LocalDateTime.now(), null);
    }

    protected abstract PolicyCollectionItem toItem(JsonNode node);

    protected String firstText(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode found = node.findValue(name);
            if (found != null && !found.isMissingNode() && !found.isNull()) {
                String value = found.asText();
                if (value != null && !value.isBlank() && !"null".equalsIgnoreCase(value)) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private List<JsonNode> listNodes(JsonNode root) {
        JsonNode item = root.findValue("item");
        if (item == null) {
            item = root.findValue("items");
        }
        if (item == null) {
            item = root.findValue("youthPolicyList");
        }
        if (item == null) {
            return List.of();
        }
        if (item.isArray()) {
            List<JsonNode> nodes = new ArrayList<>();
            item.forEach(nodes::add);
            return nodes;
        }
        return List.of(item);
    }

    private int intValue(JsonNode root, String... names) {
        for (String name : names) {
            JsonNode found = root.findValue(name);
            if (found != null && found.canConvertToInt()) {
                return found.asInt();
            }
        }
        return 0;
    }

    private String stableId(PolicySource source, String name, String organization, List<String> regions) {
        return source.name() + ":" + Math.abs(Objects.hash(name, organization, regions));
    }

    private String conditionText(String condition, String keyword) {
        return condition != null && condition.contains(keyword) ? condition : null;
    }
}
