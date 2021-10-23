package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.QueueConsumer;
import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
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

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class QueueConsumerFactory {

    private final ClientSessionFactory clientSessionFactory;

    /**
     * Creates a new {@link ClientConsumer} for a given {@link Queue}. A new Artemis socket is being opened in the
     * background for this.
     *
     * @param queue the queue to create the consumer for
     * @return the freshly created consumer
     */
    public QueueConsumer createConsumer(final Queue queue) {
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
}
