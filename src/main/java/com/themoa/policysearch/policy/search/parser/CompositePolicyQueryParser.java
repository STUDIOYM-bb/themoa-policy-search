package com.themoa.policysearch.policy.search.parser;

import org.springframework.stereotype.Component;

@Component
public class CompositePolicyQueryParser implements PolicyQueryParser {
    private final OpenAiPolicyQueryParser openAiParser;
    private final RuleBasedPolicyQueryParser ruleBasedParser;

    public CompositePolicyQueryParser(OpenAiPolicyQueryParser openAiParser, RuleBasedPolicyQueryParser ruleBasedParser) {
        this.openAiParser = openAiParser;
        this.ruleBasedParser = ruleBasedParser;
    }

    @Override
    public PolicyQueryParseResult parseQuery(String query) {
        try {
            return PolicyQueryParseResult.openAi(openAiParser.parse(query));
        } catch (RuntimeException ex) {
            return PolicyQueryParseResult.ruleBased(ruleBasedParser.parse(query), safeReason(ex));
        }
    }

    private String safeReason(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "OpenAI 조건 분석을 사용할 수 없어 규칙 기반 분석을 사용했습니다.";
        }
        return "OpenAI 조건 분석 실패: " + message;
    }
}
