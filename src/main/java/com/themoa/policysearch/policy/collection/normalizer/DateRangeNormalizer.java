package com.themoa.policysearch.policy.collection.normalizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class DateRangeNormalizer {
    private static final Pattern DATE = Pattern.compile("(20\\d{2})[.\\-/년 ]\\s*(\\d{1,2})[.\\-/월 ]\\s*(\\d{1,2})");

    public DateRange normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return new DateRange(null, null, raw, false);
        }
        if (raw.contains("상시") || raw.contains("수시")) {
            return new DateRange(null, null, raw, true);
        }
        Matcher matcher = DATE.matcher(raw);
        LocalDate first = null;
        LocalDate second = null;
        while (matcher.find()) {
            LocalDate parsed = LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)));
            if (first == null) {
                first = parsed;
            } else {
                second = parsed;
                break;
            }
        }
        return new DateRange(first, second, raw, false);
    }

    public String format(DateRange range) {
        if (range.alwaysOpen()) {
            return "상시 신청";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        if (range.startDate() != null && range.endDate() != null) {
            return formatter.format(range.startDate()) + " ~ " + formatter.format(range.endDate());
        }
        return range.rawText() == null || range.rawText().isBlank() ? "확인 필요" : range.rawText();
    }
}
