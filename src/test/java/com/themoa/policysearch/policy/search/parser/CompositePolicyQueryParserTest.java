package com.themoa.policysearch.policy.search.parser;

import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompositePolicyQueryParserTest {
    private final RuleBasedPolicyQueryParser ruleBasedParser = new RuleBasedPolicyQueryParser();

    @Test
    void usesOpenAiParserWhenSuccessful() {
        OpenAiPolicyQueryParser openAiParser = mock(OpenAiPolicyQueryParser.class);
        PolicySearchCondition condition = new PolicySearchCondition();
        condition.setRegion("경기도 수원시");
        when(openAiParser.parse("수원 청년 지원")).thenReturn(condition);

        PolicyQueryParseResult result = new CompositePolicyQueryParser(openAiParser, ruleBasedParser)
                .parseQuery("수원 청년 지원");

        assertThat(result.parserMode()).isEqualTo(ParserMode.OPENAI);
        assertThat(result.fallback()).isFalse();
        assertThat(result.condition().getRegion()).isEqualTo("경기도 수원시");
    }

    @Test
    void fallsBackToRuleBasedParserWhenOpenAiFails() {
        OpenAiPolicyQueryParser openAiParser = mock(OpenAiPolicyQueryParser.class);
        when(openAiParser.parse("수원 사는 27살 무직 청년")).thenThrow(new IllegalStateException("timeout"));

        PolicyQueryParseResult result = new CompositePolicyQueryParser(openAiParser, ruleBasedParser)
                .parseQuery("수원 사는 27살 무직 청년");

        assertThat(result.parserMode()).isEqualTo(ParserMode.RULE_BASED);
        assertThat(result.fallback()).isTrue();
        assertThat(result.fallbackReason()).contains("OpenAI 조건 분석 실패");
        assertThat(result.condition().getRegion()).isEqualTo("경기도 수원시");
        assertThat(result.condition().getAge()).isEqualTo(27);
    }
}
