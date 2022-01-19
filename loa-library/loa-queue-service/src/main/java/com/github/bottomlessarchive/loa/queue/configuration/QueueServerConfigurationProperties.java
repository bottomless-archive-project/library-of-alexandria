package com.github.bottomlessarchive.loa.queue.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.queue")
public record QueueServerConfigurationProperties(

        int port,
        String dataDirectory
) {
}
