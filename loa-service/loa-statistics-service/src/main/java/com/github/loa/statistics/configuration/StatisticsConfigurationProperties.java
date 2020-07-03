package com.github.loa.statistics.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.statistics")
public class StatisticsConfigurationProperties {

    private String collectionRate;
}
