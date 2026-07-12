package com.themoa.policysearch.policy.collection.normalizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class AgeConditionNormalizer {
    private static final Pattern RANGE = Pattern.compile("(\\d{1,2})\\s*(?:세|살).*?(\\d{1,2})\\s*(?:세|살)");

    public AgeRange normalize(String raw) {
        if (raw == null) {
            return new AgeRange(null, null);
        }
        Matcher matcher = RANGE.matcher(raw);
        if (matcher.find()) {
            int first = Integer.parseInt(matcher.group(1));
            int second = Integer.parseInt(matcher.group(2));
            return new AgeRange(Math.min(first, second), Math.max(first, second));
        }
        if (raw.contains("청년")) {
            return new AgeRange(19, 34);
        }
        return new AgeRange(null, null);
    }

    public record AgeRange(Integer minimumAge, Integer maximumAge) {
    }
}
