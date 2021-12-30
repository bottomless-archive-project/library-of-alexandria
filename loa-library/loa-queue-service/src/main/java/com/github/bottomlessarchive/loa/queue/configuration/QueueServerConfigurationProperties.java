package com.github.bottomlessarchive.loa.queue.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties("loa.queue")
public class QueueServerConfigurationProperties {

    private final int port;
    private final String dataDirectory;
}
