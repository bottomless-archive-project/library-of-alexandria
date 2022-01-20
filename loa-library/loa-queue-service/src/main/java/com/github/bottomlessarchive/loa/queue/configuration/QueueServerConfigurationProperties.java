package com.github.bottomlessarchive.loa.queue.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loa.queue")
public record QueueServerConfigurationProperties(

        int port,
        String dataDirectory,
        int producerPoolSize,
        int consumerPoolSize
) {

    public QueueServerConfigurationProperties(final int port, final String dataDirectory, final int producerPoolSize,
            final int consumerPoolSize) {
        this.port = port;
        this.dataDirectory = dataDirectory;
        this.producerPoolSize = producerPoolSize > 0 ? producerPoolSize : 10;
        this.consumerPoolSize = consumerPoolSize > 0 ? consumerPoolSize : 10;
    }
}
