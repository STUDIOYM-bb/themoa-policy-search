package com.themoa.policysearch.policy.rag.document;

import java.util.Map;

public record PolicyDocument(String id, String content, String contentHash, Map<String, Object> metadata) {
}
