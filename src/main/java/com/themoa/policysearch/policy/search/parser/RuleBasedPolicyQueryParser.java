package com.themoa.policysearch.policy.search.parser;

import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedPolicyQueryParser {
    private static final Pattern AGE = Pattern.compile("(\\d{1,2})\\s*(살|세)");

    public PolicySearchCondition parse(String query) {
        PolicySearchCondition condition = new PolicySearchCondition();
        String text = query == null ? "" : query;
        Matcher age = AGE.matcher(text);
        if (age.find()) {
            condition.setAge(Integer.parseInt(age.group(1)));
        }
        if (contains(text, "수원", "수원시")) condition.setRegion("경기도 수원시");
        else if (contains(text, "경기", "경기도")) condition.setRegion("경기도");
        else if (contains(text, "서울", "서울시")) condition.setRegion("서울특별시");
        else if (text.contains("광주")) condition.setRegion("광주");

        List<String> targets = new ArrayList<>();
        if (text.contains("청년")) {
            condition.setAgeGroup("청년");
            targets.add("청년");
        }
        if (text.contains("신혼부부")) targets.add("신혼부부");
        if (contains(text, "저소득", "차상위")) targets.add("저소득층");
        if (contains(text, "구직자", "취준생")) targets.add("구직자");
        condition.setTargetGroups(targets);

        if (contains(text, "무직", "미취업", "취준생")) condition.setEmploymentStatus("UNEMPLOYED");
        if (contains(text, "재직", "직장인")) condition.setEmploymentStatus("EMPLOYED");
        if (contains(text, "대학생", "휴학생", "졸업생")) condition.setStudentStatus(true);
        if (contains(text, "월세", "전세", "무주택")) condition.setHousingStatus(text.contains("무주택") ? "무주택" : "임차");

        List<String> keywords = new ArrayList<>();
        if (contains(text, "주거", "월세", "전세")) condition.setCategory("주거");
        if (contains(text, "취업", "일자리")) condition.setCategory("일자리");
        if (contains(text, "금융", "적금", "대출")) condition.setCategory("금융");
        if (contains(text, "교육", "장학")) condition.setCategory("교육");
        if (contains(text, "창업")) condition.setCategory("창업");
        if (contains(text, "생활비", "지원금")) condition.setCategory("생활지원");
        for (String keyword : List.of("생활비", "지원금", "월세", "취업", "교육", "청년", "주거")) {
            if (text.contains(keyword)) keywords.add(keyword);
        }
        condition.setKeywords(keywords);
        return condition;
    }

    private boolean contains(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) return true;
        }
        return false;
    }
}
