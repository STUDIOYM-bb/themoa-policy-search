package com.themoa.policysearch.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PolicyCollectionProperties.class)
public class AppConfig {
}
