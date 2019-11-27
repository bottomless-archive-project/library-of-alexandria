package com.github.loa.queue.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.queue")
public class QueueConfigurationProperties {

    private String host;
    private int port;
}
