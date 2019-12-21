package com.github.loa.generator.configuration;

import com.github.loa.queue.artemis.service.domain.ClientProducerRegistryBean;
import com.github.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneratorConfiguration {

    @Bean
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) throws ActiveMQException {
        return clientSessionFactory.createSession();
    }

    @Bean
    public ClientProducerRegistryBean clientProducer(final ClientSession clientSession) throws ActiveMQException {
        final ClientProducer clientProducer = clientSession.createProducer(Queue.DOCUMENT_LOCATION_QUEUE.getAddress());

        return ClientProducerRegistryBean.builder()
                .queue(Queue.DOCUMENT_LOCATION_QUEUE)
                .clientProducer(clientProducer)
                .build();
    }
}
