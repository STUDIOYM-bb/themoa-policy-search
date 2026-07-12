package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.policy.domain.PolicySource;

public class PolicyApiParseException extends PolicyApiException {
    private final PolicySource source;
    private final int page;
    private final String parserType;
    private final String responseContentType;
    private final String responsePreview;
    private final String rawBody;
    private final String maskedRequestUrl;

    public PolicyApiParseException(PolicySource source, int page, String parserType, String responseContentType,
                                   String rawBody, String maskedRequestUrl, Throwable cause) {
        super(buildMessage(source, page, parserType, responseContentType, rawBody, cause), cause);
        this.source = source;
        this.page = page;
        this.parserType = parserType;
        this.responseContentType = responseContentType;
        this.rawBody = rawBody;
        this.responsePreview = preview(rawBody);
        this.maskedRequestUrl = maskedRequestUrl;
    }

    public PolicyApiParseException(PolicySource source, int page, String parserType, String responseContentType,
                                   String rawBody, String maskedRequestUrl, String message) {
        super(buildMessage(source, page, parserType, responseContentType, rawBody, message));
        this.source = source;
        this.page = page;
        this.parserType = parserType;
        this.responseContentType = responseContentType;
        this.rawBody = rawBody;
        this.responsePreview = preview(rawBody);
        this.maskedRequestUrl = maskedRequestUrl;
    }

    private static String buildMessage(PolicySource source, int page, String parserType, String responseContentType,
                                       String rawBody, Throwable cause) {
        return buildMessage(source, page, parserType, responseContentType, rawBody,
                cause.getClass().getSimpleName() + ": " + cause.getMessage());
    }

    private static String buildMessage(PolicySource source, int page, String parserType, String responseContentType,
                                       String rawBody, String reason) {
        String hint = "";
        if (rawBody == null || rawBody.isBlank()) {
            hint = " 외부 API가 빈 응답을 반환했습니다.";
        } else if (looksHtml(rawBody)) {
            hint = " 예상한 JSON/XML 대신 HTML 응답을 받았습니다. 인증 실패, 잘못된 URL 또는 서비스 점검 여부를 확인하세요.";
        }
        return "정책 API 응답 파싱 실패: source=" + source
                + ", page=" + page
                + ", parserType=" + parserType
                + ", responseContentType=" + nullToEmpty(responseContentType)
                + ", cause=" + reason
                + ", responsePreview=" + preview(rawBody)
                + hint;
    }

    private static boolean looksHtml(String body) {
        String trimmed = body == null ? "" : body.stripLeading().toLowerCase();
        return trimmed.startsWith("<!doctype html") || trimmed.startsWith("<html");
    }

    private static String preview(String body) {
        if (body == null) {
            return "";
        }
        String masked = body.replaceAll("(?i)(serviceKey|openApiVlak)=([^&\\s<]+)", "$1=****");
        return masked.length() > 1000 ? masked.substring(0, 1000) : masked;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public PolicySource source() { return source; }
    public int page() { return page; }
    public String parserType() { return parserType; }
    public String responseContentType() { return responseContentType; }
    public String responsePreview() { return responsePreview; }
    public String rawBody() { return rawBody; }
    public String maskedRequestUrl() { return maskedRequestUrl; }
}
