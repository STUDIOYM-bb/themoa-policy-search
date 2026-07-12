package com.themoa.policysearch.policy.collection.scheduler;

import com.themoa.policysearch.common.config.PolicyCollectionProperties;
import com.themoa.policysearch.policy.collection.service.PolicyCollectionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PolicyCollectionScheduler {
    private final PolicyCollectionProperties properties;
    private final PolicyCollectionService collectionService;

    public PolicyCollectionScheduler(PolicyCollectionProperties properties, PolicyCollectionService collectionService) {
        this.properties = properties;
        this.collectionService = collectionService;
    }

    @Scheduled(cron = "${app.policy.collection.cron}", zone = "Asia/Seoul")
    public void collectDaily() {
        if (properties.isEnabled()) {
            collectionService.collectAll("SCHEDULED");
        }
    }
}
