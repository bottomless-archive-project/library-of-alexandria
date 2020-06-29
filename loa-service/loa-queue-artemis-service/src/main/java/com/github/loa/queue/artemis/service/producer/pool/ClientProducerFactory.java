package com.github.loa.queue.artemis.service.producer.pool;

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

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientProducerFactory {

    private final ClientSessionFactory clientSessionFactory;

    /**
     * Creates a new {@link ClientProducer} for a given {@link Queue}. A new Artemis socket is being opened in the
     * background for this.
     *
     * @param queue the queue to create the producer for
     * @return the freshly created producer
     */
    public ClientProducer createProducer(final Queue queue) {
        log.info("Creating new producer for queue: " + queue + "!");

        try {
            final ClientSession clientSession = clientSessionFactory.createSession();

            return clientSession.createProducer(queue.getAddress());
        } catch (final ActiveMQException e) {
            log.error("Error while creating client producer for queue {}!", queue, e);

            throw new QueueException("Unable to create client producer for queue: " + queue + "!");
        }
    }
}