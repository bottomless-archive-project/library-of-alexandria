package com.github.bottomlessarchive.loa.statistics.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.statistics")
public record StatisticsConfigurationProperties(

        String collectionRate
) {
}
