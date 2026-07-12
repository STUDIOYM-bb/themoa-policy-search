package com.themoa.policysearch.policy.collection.normalizer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class KeywordNormalizer {
    private static final List<String> KEYWORDS = List.of(
            "청년", "월세", "전세", "주거", "취업", "구직", "무직", "대학생", "지원금",
            "생활비", "교육", "창업", "복지", "저소득", "무주택", "신혼부부");

    public List<String> normalize(String... texts) {
        Set<String> result = new LinkedHashSet<>();
        for (String text : texts) {
            if (text == null) {
                continue;
            }
            for (String keyword : KEYWORDS) {
                if (text.contains(keyword)) {
                    result.add(keyword);
                }
            }
        }
        return List.copyOf(result);
    }
}
