package com.themoa.policysearch.policy.rag.document;

import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicyCondition;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class PolicyDocumentBuilder {
    public PolicyDocument build(Policy policy) {
        List<String> regions = policy.getRegions().stream().map(pr -> pr.getRegion().displayName()).toList();
        List<String> regionCodes = policy.getRegions().stream().map(pr -> pr.getRegion().getRegionCode()).toList();
        PolicyCondition condition = policy.getCondition();
        StringBuilder content = new StringBuilder();
        append(content, "정책명", policy.getTitle());
        append(content, "분야", policy.getCategory().name());
        append(content, "지역", String.join(", ", regions));
        append(content, "핵심 내용", policy.getSummary());
        if (condition != null) {
            append(content, "연령 조건", age(condition));
            append(content, "취업 조건", condition.getEmploymentStatus());
            append(content, "학생 조건", condition.getStudentStatus() == null ? null : (condition.getStudentStatus() ? "학생 대상" : "학생 제한 없음"));
            append(content, "소득 조건", condition.getIncomeCondition());
            append(content, "선정 기준", condition.getConditionSummary());
        }
        append(content, "신청 기간", period(policy));
        append(content, "기관", policy.getAgencyName());
        String body = content.toString().trim();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("policyId", policy.getId());
        metadata.put("sourcePolicyIds", List.of(policy.getSourcePolicyId()));
        metadata.put("source", policy.getSourceType().name());
        metadata.put("category", policy.getCategory().name());
        metadata.put("regionCodes", regionCodes);
        metadata.put("regionNames", regions);
        metadata.put("applicationStatus", policy.getStatus());
        metadata.put("active", policy.isActive());
        metadata.put("applicationStartDate", policy.getStartDate() == null ? null : policy.getStartDate().toString());
        metadata.put("applicationEndDate", policy.getDueDate() == null ? null : policy.getDueDate().toString());
        if (condition != null) {
            metadata.put("minimumAge", condition.getMinAge());
            metadata.put("maximumAge", condition.getMaxAge());
            metadata.put("employmentStatus", condition.getEmploymentStatus());
            metadata.put("studentStatus", condition.getStudentStatus());
        }
        String contentHash = sha256(body);
        metadata.put("contentHash", contentHash);
        removeNullValues(metadata);
        return new PolicyDocument(documentId(policy.getId()), body, contentHash, metadata);
    }

    private void append(StringBuilder builder, String label, String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return;
        }
        builder.append('[').append(label).append(']').append('\n').append(value.trim()).append("\n\n");
    }

    private String age(PolicyCondition condition) {
        if (condition.getMinAge() == null && condition.getMaxAge() == null) {
            return null;
        }
        if (condition.getMinAge() != null && condition.getMaxAge() != null) {
            return "만 " + condition.getMinAge() + "세 이상 만 " + condition.getMaxAge() + "세 이하";
        }
        return condition.getMinAge() != null ? "만 " + condition.getMinAge() + "세 이상" : "만 " + condition.getMaxAge() + "세 이하";
    }

    private String period(Policy policy) {
        if (policy.isAlwaysOpen()) {
            return "상시 신청";
        }
        if (policy.getStartDate() != null && policy.getDueDate() != null) {
            return policy.getStartDate() + " ~ " + policy.getDueDate();
        }
        return null;
    }

    private String sha256(String value) {
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

    private String documentId(Integer policyId) {
        return UUID.nameUUIDFromBytes(("policy:" + policyId).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private void removeNullValues(Map<String, Object> metadata) {
        metadata.entrySet().removeIf(entry -> entry.getValue() == null);
    }
}
