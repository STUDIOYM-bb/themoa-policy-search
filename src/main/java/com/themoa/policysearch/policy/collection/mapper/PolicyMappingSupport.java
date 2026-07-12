package com.themoa.policysearch.policy.collection.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.collection.normalizer.AgeConditionNormalizer;
import com.themoa.policysearch.policy.collection.normalizer.CategoryNormalizer;
import com.themoa.policysearch.policy.collection.normalizer.DateRange;
import com.themoa.policysearch.policy.collection.normalizer.DateRangeNormalizer;
import com.themoa.policysearch.policy.collection.normalizer.KeywordNormalizer;
import com.themoa.policysearch.policy.collection.normalizer.RegionNormalizer;
import com.themoa.policysearch.policy.domain.PolicyCategory;
import com.themoa.policysearch.policy.domain.PolicySource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public abstract class PolicyMappingSupport {
    private final RegionNormalizer regionNormalizer;
    private final CategoryNormalizer categoryNormalizer;
    private final DateRangeNormalizer dateRangeNormalizer;
    private final AgeConditionNormalizer ageConditionNormalizer;
    private final KeywordNormalizer keywordNormalizer;

    protected PolicyMappingSupport(RegionNormalizer regionNormalizer, CategoryNormalizer categoryNormalizer,
                                   DateRangeNormalizer dateRangeNormalizer, AgeConditionNormalizer ageConditionNormalizer,
                                   KeywordNormalizer keywordNormalizer) {
        this.regionNormalizer = regionNormalizer;
        this.categoryNormalizer = categoryNormalizer;
        this.dateRangeNormalizer = dateRangeNormalizer;
        this.ageConditionNormalizer = ageConditionNormalizer;
        this.keywordNormalizer = keywordNormalizer;
    }

    protected PolicyCollectionItem item(PolicySource source, String id, String name, String organization,
                                        String regionRaw, String categoryRaw, String target, String support,
                                        String criteria, String method, String requiredDocuments,
                                        String period, String url, String contact) {
        DateRange dateRange = dateRangeNormalizer.normalize(period);
        AgeConditionNormalizer.AgeRange age = ageConditionNormalizer.normalize(join(target, criteria));
        PolicyCategory category = categoryNormalizer.normalize(join(categoryRaw, name, support));
        List<String> regions = regionNormalizer.normalize(regionRaw);
        List<String> keywords = keywordNormalizer.normalize(name, target, support, criteria, categoryRaw);
        String stableId = (id == null || id.isBlank()) ? stableId(source, name, organization, regions) : id.trim();
        return new PolicyCollectionItem(source, stableId, name, defaultText(organization, "확인 필요"),
                organization, regions, List.of(), category, target == null ? List.of() : List.of(target),
                age.minimumAge(), age.maximumAge(), containsText(criteria, "취업"), contains(criteria, "학생"),
                containsText(criteria, "소득"), containsText(criteria, "주거"), containsText(criteria, "가구"),
                criteria, support, method, requiredDocuments, dateRange.startDate(), dateRange.endDate(),
                dateRange.rawText(), dateRange.alwaysOpen(), url, contact, keywords, null,
                LocalDateTime.now(), null);
    }

    protected String text(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() || "null".equalsIgnoreCase(text) ? null : text.trim();
    }

    protected String firstText(JsonNode node, String... names) {
        for (String name : names) {
            String value = text(node, name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    protected String join(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(value);
            }
        }
        return builder.toString();
    }

    private String defaultText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private Boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword) ? true : null;
    }

    private String containsText(String value, String keyword) {
        return value != null && value.contains(keyword) ? value : null;
    }

    private String stableId(PolicySource source, String name, String organization, List<String> regions) {
        return source.name() + ":" + Math.abs(Objects.hash(name, organization, regions));
    }
}
