package com.themoa.policysearch.policy.search.parser;

import com.themoa.policysearch.policy.collection.normalizer.RegionNormalizer;
import com.themoa.policysearch.policy.search.dto.PolicySearchCondition;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiPolicyQueryParser {
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final RegionNormalizer regionNormalizer;
    private final boolean ragEnabled;

    public OpenAiPolicyQueryParser(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                                   RegionNormalizer regionNormalizer,
                                   @Value("${app.rag.enabled:false}") boolean ragEnabled) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.regionNormalizer = regionNormalizer;
        this.ragEnabled = ragEnabled;
    }

    public PolicySearchCondition parse(String query) {
        if (!ragEnabled) {
            throw new IllegalStateException("RAG가 비활성화되어 OpenAI 조건 분석을 사용하지 않습니다.");
        }
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException("OpenAI ChatClient가 구성되지 않았습니다.");
        }
        PolicySearchCondition condition = builder.build()
                .prompt()
                .system("""
                        당신은 한국 정책 검색 조건 추출기입니다.
                        정책을 추천하거나 생성하지 말고 사용자 문장에서 검색 조건만 추출하세요.
                        알 수 없는 값은 null 또는 빈 배열로 둡니다.
                        requestedApplicationStatus는 사용자가 마감/예정/상시를 명시하지 않으면 OPEN으로 둡니다.
                        지역은 가능한 경우 '경기도 수원시', '경기도', '서울특별시', '전국'처럼 표준명으로 답하세요.
                        employmentStatus는 EMPLOYED, UNEMPLOYED, JOB_SEEKER, FREELANCER 중 하나를 우선 사용하세요.
                        """)
                .user(query)
                .call()
                .entity(PolicySearchCondition.class);
        if (condition == null) {
            throw new IllegalStateException("OpenAI 조건 분석 결과가 비어 있습니다.");
        }
        normalize(condition);
        return condition;
    }

    private void normalize(PolicySearchCondition condition) {
        if (condition.getRegion() != null && !condition.getRegion().isBlank()) {
            condition.setRegion(regionNormalizer.normalize(condition.getRegion()).get(0));
        }
        if (condition.getTargetGroups() == null) {
            condition.setTargetGroups(new ArrayList<>());
        }
        if (condition.getKeywords() == null) {
            condition.setKeywords(new ArrayList<>());
        }
        condition.setKeywords(condition.getKeywords().stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList());
        condition.setTargetGroups(condition.getTargetGroups().stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList());
        if (condition.getRequestedApplicationStatus() == null || condition.getRequestedApplicationStatus().isBlank()) {
            condition.setRequestedApplicationStatus("OPEN");
        }
    }
}
