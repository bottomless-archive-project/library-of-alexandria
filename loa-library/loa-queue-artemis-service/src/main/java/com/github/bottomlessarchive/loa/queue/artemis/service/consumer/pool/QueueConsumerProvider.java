package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.QueueConsumer;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
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
public class QueueConsumerProvider implements Closeable {

    private final ClientSessionFactory clientSessionFactory;

    private final Map<Queue, QueueConsumer> clientConsumers = new EnumMap<>(Queue.class);

    /**
     * Provides a {@link ClientConsumer} for a given {@link Queue}. A new Artemis socket might be opened in the
     * background for this.
     *
     * @param queue the queue to return the consumer for
     * @return the consumer
     */
    public QueueConsumer getConsumer(final Queue queue) {
        return clientConsumers.computeIfAbsent(queue, this::createConsumer);
    }

    private QueueConsumer createConsumer(final Queue queue) {
        log.info("Creating new consumer for queue: {}!", queue);

        try {
            final ClientSession clientSession = clientSessionFactory.createSession();
            final ClientConsumer clientConsumer = clientSession.createConsumer(queue.getAddress());

            // Pre-start the consumer so we don't need to synchronize later on
            clientSession.start();

            return QueueConsumer.builder()
                    .clientSession(clientSession)
                    .clientConsumer(clientConsumer)
                    .build();
        } catch (final ActiveMQException e) {
            log.error("Error while creating client consumer for queue {}!", queue, e);

            throw new QueueException("Unable to create client consumer for queue: " + queue + "!", e);
        }
    }

    @Override
    public void close() {
        clientConsumers.values()
                .forEach(QueueConsumer::close);
    }
}
