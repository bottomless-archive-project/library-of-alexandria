package com.github.loa.queue.service;

import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArtemisQueueInitializer implements CommandLineRunner {

    private final EmbeddedActiveMQ embeddedActiveMQ;

    @Override
    public void run(final String... args) throws Exception {
        embeddedActiveMQ.start();
    }
}
