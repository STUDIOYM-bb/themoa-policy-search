package com.themoa.policysearch.policy.collection.client;

public class PolicyApiException extends RuntimeException {
    public PolicyApiException(String message) {
        super(message);
    }

    public PolicyApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
