package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientProducerProvider {

    private final ClientSessionFactory clientSessionFactory;
    private final Map<Queue, List<ClientProducer>> clientProducers = new EnumMap<>(Queue.class);

    public void invokeProducer(final Queue queue, final Consumer<ClientProducer> clientProducerConsumer) {
        if (!clientProducers.containsKey(queue)) {
            clientProducers.put(queue, new ArrayList<>());
        }

        final List<ClientProducer> clientProducersForQueue = clientProducers.get(queue);

        ClientProducer clientProducer;
        synchronized (clientProducers) {
            if (clientProducersForQueue.size() > 0) {
                clientProducer = clientProducersForQueue.remove(0);
            } else {
                clientProducer = createProducer(queue);
            }
        }

        clientProducerConsumer.accept(clientProducer);

        clientProducers.get(queue).add(clientProducer);
    }

    private ClientProducer createProducer(final Queue queue) {
        log.info("Creating new producer for queue: " + queue + "!");

        try {
            final ClientSession clientSession = clientSessionFactory.createSession();

            return clientSession.createProducer(queue.getAddress());
        } catch (ActiveMQException e) {
            throw new QueueException("Unable to create client producer for queue: " + queue + "!");
        }
    }
}
