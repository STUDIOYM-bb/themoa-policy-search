package com.themoa.policysearch.policy.search.evaluator;

import com.themoa.policysearch.policy.collection.normalizer.RegionNormalizer;
import com.themoa.policysearch.policy.domain.EligibilityStatus;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicyCondition;
import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PolicyEligibilityEvaluator {
    private final RegionNormalizer regionNormalizer;

    public PolicyEligibilityEvaluator(RegionNormalizer regionNormalizer) {
        this.regionNormalizer = regionNormalizer;
    }

    public Evaluation evaluate(Policy policy, PolicySearchCondition condition) {
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<String> unmatched = new ArrayList<>();
        List<String> regions = policy.getRegions().stream().map(pr -> pr.getRegion().displayName()).toList();
        if (condition.getRegion() != null) {
            if (regionNormalizer.matchesStrict(condition.getRegion(), regions)) matched.add("region");
            else unmatched.add("region");
        } else {
            missing.add("region");
        }
        PolicyCondition pc = policy.getCondition();
        if (pc != null) {
            if (condition.getAge() != null && pc.getMinAge() != null && condition.getAge() < pc.getMinAge()) unmatched.add("age");
            else if (condition.getAge() != null && pc.getMaxAge() != null && condition.getAge() > pc.getMaxAge()) unmatched.add("age");
            else if (condition.getAge() != null && (pc.getMinAge() != null || pc.getMaxAge() != null)) matched.add("age");
            else if (pc.getMinAge() != null || pc.getMaxAge() != null) missing.add("age");

            if (pc.getEmploymentStatus() != null) {
                if (condition.getEmploymentStatus() == null) missing.add("employmentStatus");
                else matched.add("employmentStatus");
            }
            if (pc.getIncomeCondition() != null && condition.getIncomeCondition() == null) missing.add("incomeCondition");
        }
        EligibilityStatus status;
        if (!unmatched.isEmpty()) status = EligibilityStatus.UNLIKELY;
        else if (missing.isEmpty() && matched.size() >= 2) status = EligibilityStatus.ELIGIBLE;
        else if (!matched.isEmpty()) status = EligibilityStatus.LIKELY_ELIGIBLE;
        else status = EligibilityStatus.NEEDS_CONFIRMATION;
        return new Evaluation(status, matched, missing, unmatched);
    }

    public record Evaluation(EligibilityStatus status, List<String> matchedConditions, List<String> missingConditions,
                             List<String> unmatchedConditions) {
    }
}
