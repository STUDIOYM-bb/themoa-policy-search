package com.themoa.policysearch.policy.collection.client;

import com.themoa.policysearch.policy.domain.PolicySource;

public class PolicyApiResponseException extends PolicyApiException {
    private final PolicySource source;
    private final int statusCode;
    private final String contentType;
    private final String apiErrorCode;
    private final String apiErrorMessage;
    private final String maskedRequestUrl;
    private final String responseBody;

    public PolicyApiResponseException(PolicySource source, int statusCode, String contentType,
                                      String apiErrorCode, String apiErrorMessage,
                                      String maskedRequestUrl, String responseBody) {
        super(buildMessage(source, statusCode, contentType, apiErrorCode, apiErrorMessage, maskedRequestUrl));
        this.source = source;
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.apiErrorCode = apiErrorCode;
        this.apiErrorMessage = apiErrorMessage;
        this.maskedRequestUrl = maskedRequestUrl;
        this.responseBody = responseBody;
    }

    public PolicyApiResponseException(PolicySource source, int statusCode, String contentType,
                                      String apiErrorCode, String apiErrorMessage,
                                      String maskedRequestUrl, String responseBody, Throwable cause) {
        super(buildMessage(source, statusCode, contentType, apiErrorCode, apiErrorMessage, maskedRequestUrl), cause);
        this.source = source;
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.apiErrorCode = apiErrorCode;
        this.apiErrorMessage = apiErrorMessage;
        this.maskedRequestUrl = maskedRequestUrl;
        this.responseBody = responseBody;
    }

    private static String buildMessage(PolicySource source, int statusCode, String contentType,
                                       String apiErrorCode, String apiErrorMessage, String maskedRequestUrl) {
        return "정책 API 오류 응답: source=" + source
                + ", httpStatus=" + statusCode
                + ", contentType=" + nullToEmpty(contentType)
                + ", apiErrorCode=" + nullToEmpty(apiErrorCode)
                + ", apiErrorMessage=" + nullToEmpty(apiErrorMessage)
                + ", requestUrl=" + maskedRequestUrl;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public PolicySource source() { return source; }
    public int statusCode() { return statusCode; }
    public String contentType() { return contentType; }
    public String apiErrorCode() { return apiErrorCode; }
    public String apiErrorMessage() { return apiErrorMessage; }
    public String maskedRequestUrl() { return maskedRequestUrl; }
    public String responseBody() { return responseBody; }
}
