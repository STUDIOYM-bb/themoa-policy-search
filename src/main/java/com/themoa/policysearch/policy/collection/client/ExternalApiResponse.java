package com.themoa.policysearch.policy.collection.client;

public record ExternalApiResponse(
        int statusCode,
        String contentType,
        String body,
        String maskedRequestUrl
) {
}
