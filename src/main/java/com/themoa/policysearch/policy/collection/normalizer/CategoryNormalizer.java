package com.themoa.policysearch.policy.collection.normalizer;

import com.themoa.policysearch.policy.domain.PolicyCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryNormalizer {
    public PolicyCategory normalize(String raw) {
        String text = raw == null ? "" : raw;
        if (contains(text, "주거", "월세", "전세", "임차")) return PolicyCategory.주거;
        if (contains(text, "취업", "일자리", "구직", "고용", "채용")) return PolicyCategory.일자리;
        if (contains(text, "교육", "학습", "장학", "훈련")) return PolicyCategory.교육;
        if (contains(text, "금융", "대출", "적금", "저축", "자산")) return PolicyCategory.금융;
        if (contains(text, "창업", "사업")) return PolicyCategory.창업;
        if (contains(text, "문화", "예술")) return PolicyCategory.문화;
        if (contains(text, "건강", "의료", "보건")) return PolicyCategory.건강;
        if (contains(text, "돌봄", "보육", "양육")) return PolicyCategory.돌봄;
        if (contains(text, "생활", "지원금", "생계", "교통비")) return PolicyCategory.생활지원;
        if (contains(text, "복지")) return PolicyCategory.복지;
        return PolicyCategory.기타;
    }

    private boolean contains(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
