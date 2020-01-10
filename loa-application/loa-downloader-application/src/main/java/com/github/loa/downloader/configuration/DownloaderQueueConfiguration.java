package com.github.loa.downloader.configuration;

import com.github.loa.queue.artemis.service.domain.ClientConsumerRegistryBean;
import com.github.loa.queue.artemis.service.domain.ClientProducerRegistryBean;
import com.github.loa.queue.service.domain.Queue;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Slf4j
@Configuration
public class DownloaderQueueConfiguration {

    @Bean(destroyMethod = "stop")
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) throws ActiveMQException {
        return clientSessionFactory.createSession();
    }

    @Bean
    public ClientConsumerRegistryBean clientConsumer(final ClientSessionFactory clientSessionFactory)
            throws ActiveMQException {
        final ClientSession clientSession = clientSessionFactory.createSession();

        final ClientConsumer clientConsumer = clientSession.createConsumer(Queue.DOCUMENT_LOCATION_QUEUE.getAddress());

        try {
            clientSession.start();
        } catch (final ActiveMQException e) {
            log.error("Connection error with the Queue Application!", e);
        }

        return ClientConsumerRegistryBean.builder()
                .queue(Queue.DOCUMENT_LOCATION_QUEUE)
                .clientConsumer(clientConsumer)
                .build();
    }

    @Bean
    public ClientProducerRegistryBean clientProducer(final ClientSessionFactory clientSessionFactory)
            throws ActiveMQException {
        final ClientSession clientSession = clientSessionFactory.createSession();

        final ClientProducer clientProducer = clientSession.createProducer(Queue.DOCUMENT_ARCHIVING_QUEUE.getAddress());

        return ClientProducerRegistryBean.builder()
                .queue(Queue.DOCUMENT_ARCHIVING_QUEUE)
                .clientProducer(clientProducer)
                .build();
    }
}
