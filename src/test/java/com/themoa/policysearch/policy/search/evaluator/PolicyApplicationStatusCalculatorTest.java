package com.themoa.policysearch.policy.search.evaluator;

import com.themoa.policysearch.policy.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyApplicationStatusCalculatorTest {
    @Test
    void calculatesAlwaysOpen() {
        Policy policy = new Policy("테스트", "SRC-1", PolicySource.YOUTH_CENTER, "기관", PolicyCategory.복지);
        policy.updateBasic("테스트", "기관", PolicyCategory.복지, "요약", null, null, null, true, ApplicationStatus.ALWAYS_OPEN);
        assertThat(new PolicyApplicationStatusCalculator().calculate(policy)).isEqualTo(ApplicationStatus.ALWAYS_OPEN);
    }

    @Test
    void calculatesNeedsConfirmationWhenDatesMissing() {
        Policy policy = new Policy("테스트", "SRC-2", PolicySource.YOUTH_CENTER, "기관", PolicyCategory.복지);
        policy.updateBasic("테스트", "기관", PolicyCategory.복지, "요약", null, null, null, false, ApplicationStatus.NEEDS_CONFIRMATION);
        assertThat(new PolicyApplicationStatusCalculator().calculate(policy)).isEqualTo(ApplicationStatus.NEEDS_CONFIRMATION);
    }
}
