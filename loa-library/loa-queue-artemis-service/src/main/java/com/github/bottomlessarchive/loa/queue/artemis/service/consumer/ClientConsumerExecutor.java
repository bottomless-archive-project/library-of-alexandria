package com.github.bottomlessarchive.loa.queue.artemis.service.consumer;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.PoolableQueueConsumer;
import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientConsumerExecutor {

    private final PoolableClientConsumerPoolFactory poolableClientConsumerPoolFactory;

    public <T> T invokeConsumer(final Queue queue, final Function<ClientConsumer, T> clientConsumerConsumer) {
        try (PoolableQueueConsumer poolableClientConsumer = claimClientConsumer(queue)) {
            try {
                return clientConsumerConsumer.apply(poolableClientConsumer.getQueueConsumer().getClientConsumer());
            } catch (final QueueException e) {
                log.error("Unrecoverable error happened while reading from queue: " + queue + "! "
                        + "Closing the connection to the Queue Application and will try to reconnect.", e);

                // Artemis doesn't always properly reconnect, even when
                // org.apache.activemq.artemis.api.core.client.ServerLocator#setReconnectAttempts is set to -1 (unlimited).
                // In these cases we manually need to expire the poolable and re-request the previous method that was under applying.
                // For example and more info see: https://issues.apache.org/jira/browse/ARTEMIS-3588
                poolableClientConsumer.expire();

                return invokeConsumer(queue, clientConsumerConsumer);
            }
        }
    }

    private PoolableQueueConsumer claimClientConsumer(final Queue queue) {
        final Pool<PoolableQueueConsumer> clientConsumersForQueue = poolableClientConsumerPoolFactory.getPool(queue);

        try {
            return clientConsumersForQueue.claim(new Timeout(Integer.MAX_VALUE, TimeUnit.DAYS));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new QueueException("Unable to acquire client consumer!", e);
        }
    }
}
