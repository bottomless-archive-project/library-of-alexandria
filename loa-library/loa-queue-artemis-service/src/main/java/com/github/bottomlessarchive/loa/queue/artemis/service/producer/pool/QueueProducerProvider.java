package com.github.bottomlessarchive.loa.queue.artemis.service.producer.pool;

import com.github.bottomlessarchive.loa.queue.artemis.service.producer.pool.domain.QueueProducer;
import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class QueueProducerProvider implements Closeable {

    private final ClientSessionFactory clientSessionFactory;

    private final Map<Queue, QueueProducer> clientProducers = new EnumMap<>(Queue.class);

    /**
     * Provides a {@link QueueProducer} for a given {@link Queue}. A new Artemis socket might be opened in the
     * background for this.
     *
     * @param queue the queue to return the producer for
     * @return the producer
     */
    public QueueProducer getProducer(final Queue queue) {
        return clientProducers.computeIfAbsent(queue, this::createProducer);
    }

    private QueueProducer createProducer(final Queue queue) {
        log.info("Creating new producer for queue: {}!", queue);

        try {
            final ClientSession clientSession = clientSessionFactory.createSession();
            final ClientProducer clientProducer = clientSession.createProducer(queue.getAddress());

            return QueueProducer.builder()
                    .clientSession(clientSession)
                    .clientProducer(clientProducer)
                    .build();
        } catch (final ActiveMQException e) {
            log.error("Error while creating client producer for queue {}!", queue, e);

            throw new QueueException("Unable to create client producer for queue: " + queue + "!", e);
        }
    }

    @Override
    public void close() {
        clientProducers.values()
                .forEach(queueProducer -> {
                    try {
                        queueProducer.close();
                    } catch (final ActiveMQException e) {
                        log.error("Failed to close connection to the queue!", e);
                    }
                });
    }
}
