package com.themoa.policysearch.policy.search.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedPolicyQueryParserTest {
    @Test
    void parsesNaturalLanguageConditions() {
        RuleBasedPolicyQueryParser parser = new RuleBasedPolicyQueryParser();
        var condition = parser.parse("경기도 수원에 사는 27살 무직 청년이 받을 수 있는 생활비 지원금 알려줘");
        assertThat(condition.getRegion()).isEqualTo("경기도 수원시");
        assertThat(condition.getAge()).isEqualTo(27);
        assertThat(condition.getAgeGroup()).isEqualTo("청년");
        assertThat(condition.getEmploymentStatus()).isEqualTo("UNEMPLOYED");
        assertThat(condition.getCategory()).isEqualTo("생활지원");
        assertThat(condition.getKeywords()).contains("생활비", "지원금");
    }
}
