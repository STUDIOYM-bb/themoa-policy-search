package com.themoa.policysearch.policy.search.parser;

import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;

public record PolicyQueryParseResult(
        PolicySearchCondition condition,
        ParserMode parserMode,
        boolean fallback,
        String fallbackReason
) {
    public static PolicyQueryParseResult openAi(PolicySearchCondition condition) {
        return new PolicyQueryParseResult(condition, ParserMode.OPENAI, false, null);
    }

    public static PolicyQueryParseResult ruleBased(PolicySearchCondition condition, String fallbackReason) {
        return new PolicyQueryParseResult(condition, ParserMode.RULE_BASED, fallbackReason != null, fallbackReason);
    }
}
