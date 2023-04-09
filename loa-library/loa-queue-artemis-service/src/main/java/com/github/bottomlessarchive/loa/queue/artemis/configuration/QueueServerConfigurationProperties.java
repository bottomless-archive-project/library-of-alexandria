package com.github.bottomlessarchive.loa.queue.artemis.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Validated
@ConfigurationProperties("loa.queue")
@ConditionalOnClass(EmbeddedActiveMQ.class)
public record QueueServerConfigurationProperties(

        @Min(1)
        @Max(65535)
        int port,

        @NotNull
        Path dataDirectory
) {
}
