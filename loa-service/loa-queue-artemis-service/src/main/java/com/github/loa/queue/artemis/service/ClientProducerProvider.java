package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientProducerProvider {

    private final ClientSessionFactory clientSessionFactory;
    private final Map<Queue, ClientProducer> clientProducerMap = new EnumMap<>(Queue.class);

    public ClientProducer getClientProducer(final Queue queue) {
        return clientProducerMap.computeIfAbsent(queue, this::createProducer);
    }

    private ClientProducer createProducer(final Queue queue) {
        try {
            final ClientSession clientSession = clientSessionFactory.createSession();

            return clientSession.createProducer(queue.getAddress());
        } catch (ActiveMQException e) {
            throw new QueueException("Unable to create client producer for queue: " + queue + "!");
        }
    }
}
