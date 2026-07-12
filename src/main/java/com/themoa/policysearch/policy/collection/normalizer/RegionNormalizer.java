package com.themoa.policysearch.policy.collection.normalizer;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RegionNormalizer {
    public List<String> normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("전국");
        }
        String text = raw.replaceAll("\\s+", " ").trim();
        if (containsAny(text, "전국", "중앙부처", "전 국민", "전국민")) {
            return List.of("전국");
        }
        if (containsAny(text, "수원", "수원시")) {
            return List.of("경기도 수원시");
        }
        if (containsAny(text, "경기", "경기도")) {
            return List.of("경기도");
        }
        if (containsAny(text, "서울", "서울시", "서울특별시")) {
            return List.of("서울특별시");
        }
        if (text.contains("광주")) {
            return List.of("광주");
        }
        return List.of(text);
    }

    public boolean matchesStrict(String userRegion, List<String> policyRegions) {
        if (userRegion == null || userRegion.isBlank()) {
            return true;
        }
        if (policyRegions.contains("전국")) {
            return true;
        }
        if ("경기도 수원시".equals(userRegion)) {
            return policyRegions.contains("경기도 수원시") || policyRegions.contains("경기도");
        }
        return policyRegions.contains(userRegion);
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
