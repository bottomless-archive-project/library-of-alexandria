package com.github.loa.downloader.configuration;

import com.github.loa.queue.artemis.service.domain.ClientConsumerRegistryBean;
import com.github.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DownloaderQueueConfiguration {

    @Bean
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) throws ActiveMQException {
        return clientSessionFactory.createSession();
    }

    @Bean
    public ClientConsumerRegistryBean clientConsumer(final ClientSession clientSession) throws ActiveMQException {
        final ClientConsumer clientConsumer = clientSession.createConsumer(Queue.DOCUMENT_LOCATION_QUEUE.getAddress());

        return ClientConsumerRegistryBean.builder()
                .queue(Queue.DOCUMENT_LOCATION_QUEUE)
                .clientConsumer(clientConsumer)
                .build();
    }
}
