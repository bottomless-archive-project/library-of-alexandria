package com.github.loa.downloader.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DownloaderQueueConfiguration {

    @Bean(destroyMethod = "stop")
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) throws ActiveMQException {
        return clientSessionFactory.createSession();
    }
}
