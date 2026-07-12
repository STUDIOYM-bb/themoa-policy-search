package com.themoa.policysearch.policy.search.evaluator;

import com.themoa.policysearch.policy.collection.normalizer.RegionNormalizer;
import com.themoa.policysearch.policy.domain.Policy;
import com.themoa.policysearch.policy.domain.PolicyCategory;
import com.themoa.policysearch.policy.domain.PolicyCondition;
import com.themoa.policysearch.policy.domain.PolicySource;
import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEligibilityEvaluatorTest {
    private final PolicyEligibilityEvaluator evaluator = new PolicyEligibilityEvaluator(new RegionNormalizer());

    @Test
    void unemployedUserDoesNotMatchWorkerOnlyPolicy() {
        Policy policy = new Policy("재직 청년 지원", "P-1", PolicySource.GOV_SERVICE, "기관", PolicyCategory.일자리);
        policy.replaceCondition(new PolicyCondition(19, 39, "근로자 직장인", null, null, "재직자 대상", true));
        PolicySearchCondition condition = new PolicySearchCondition();
        condition.setEmploymentStatus("무직");
        condition.setAge(30);

        PolicyEligibilityEvaluator.Evaluation result = evaluator.evaluate(policy, condition);

        assertThat(result.unmatchedConditions()).contains("employmentStatus");
    }

    @Test
    void ageRangeIsComparedWithActualUserAge() {
        Policy policy = new Policy("청년 지원", "P-2", PolicySource.GOV_SERVICE, "기관", PolicyCategory.생활지원);
        policy.replaceCondition(new PolicyCondition(19, 34, null, null, null, "청년 대상", true));
        PolicySearchCondition condition = new PolicySearchCondition();
        condition.setAge(40);

        PolicyEligibilityEvaluator.Evaluation result = evaluator.evaluate(policy, condition);

        assertThat(result.unmatchedConditions()).contains("age");
    }
}
