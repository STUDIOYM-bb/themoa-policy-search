package com.themoa.policysearch.policy.collection.normalizer;

import java.time.LocalDate;

public record DateRange(LocalDate startDate, LocalDate endDate, String rawText, boolean alwaysOpen) {
}
