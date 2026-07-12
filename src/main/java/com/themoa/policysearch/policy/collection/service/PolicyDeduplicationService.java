package com.themoa.policysearch.policy.collection.service;

import com.themoa.policysearch.policy.collection.dto.PolicyCollectionItem;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.repository.PolicyRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PolicyDeduplicationService {
    private final PolicyRepository policyRepository;

    public PolicyDeduplicationService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public Optional<Policy> findSameSourceDuplicate(PolicyCollectionItem item) {
        if (item.sourcePolicyId() == null || item.sourcePolicyId().isBlank()) {
            return Optional.empty();
        }
        return policyRepository.findBySourceTypeAndSourcePolicyId(item.source(), item.sourcePolicyId());
    }

    public boolean isLikelySamePolicy(Policy left, PolicyCollectionItem right) {
        if (left.getSourceType() == right.source() && left.getSourcePolicyId().equals(right.sourcePolicyId())) {
            return true;
        }
        if (!normalize(left.getAgencyName()).equals(normalize(right.organization()))) {
            return false;
        }
        if (left.getDueDate() != null && right.applicationEndDate() != null && !left.getDueDate().equals(right.applicationEndDate())) {
            return false;
        }
        return similarity(left.getTitle(), right.policyName()) >= 0.85;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
    }

    double similarity(String a, String b) {
        String left = normalize(a);
        String right = normalize(b);
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        int distance = levenshtein(left, right);
        return 1.0 - (double) distance / Math.max(left.length(), right.length());
    }

    private int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        return prev[b.length()];
    }
}
