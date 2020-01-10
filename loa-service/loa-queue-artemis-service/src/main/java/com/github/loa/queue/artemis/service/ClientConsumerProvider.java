package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientConsumerProvider {

    private final ClientSessionFactory clientSessionFactory;
    private final Map<Queue, ClientConsumer> clientConsumerMap = new EnumMap<>(Queue.class);

    public ClientConsumer getClientConsumer(final Queue queue) {
        return clientConsumerMap.computeIfAbsent(queue, this::createConsumer);
    }

    private ClientConsumer createConsumer(final Queue queue) {
        try {
            final ClientSession clientSession = clientSessionFactory.createSession();
            final ClientConsumer clientConsumer = clientSession.createConsumer(queue.getAddress());

            clientSession.start();

            return clientConsumer;
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to create client consumer for queue: " + queue + "!");
        }
    }
}
