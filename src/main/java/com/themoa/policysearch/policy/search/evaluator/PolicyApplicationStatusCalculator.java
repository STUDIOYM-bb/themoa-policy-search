package com.themoa.policysearch.policy.search.evaluator;

import com.themoa.policysearch.policy.domain.ApplicationStatus;
import com.themoa.policysearch.policy.domain.Policy;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class PolicyApplicationStatusCalculator {
    private final Clock clock;

    public PolicyApplicationStatusCalculator() {
        this(Clock.systemDefaultZone());
    }

    PolicyApplicationStatusCalculator(Clock clock) {
        this.clock = clock;
    }

    public ApplicationStatus calculate(Policy policy) {
        if (!policy.isActive()) {
            return ApplicationStatus.INACTIVE;
        }
        if (policy.isAlwaysOpen()) {
            return ApplicationStatus.ALWAYS_OPEN;
        }
        LocalDate today = LocalDate.now(clock);
        if (policy.getStartDate() == null && policy.getDueDate() == null) {
            return ApplicationStatus.NEEDS_CONFIRMATION;
        }
        if (policy.getStartDate() != null && today.isBefore(policy.getStartDate())) {
            return ApplicationStatus.UPCOMING;
        }
        if (policy.getDueDate() != null && today.isAfter(policy.getDueDate())) {
            return ApplicationStatus.CLOSED;
        }
        return ApplicationStatus.OPEN;
    }
}
