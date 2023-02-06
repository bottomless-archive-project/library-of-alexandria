package com.github.bottomlessarchive.loa.queue.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("loa.queue")
public record QueueServerConfigurationProperties(

        int port,
        Path dataDirectory
) {
}
