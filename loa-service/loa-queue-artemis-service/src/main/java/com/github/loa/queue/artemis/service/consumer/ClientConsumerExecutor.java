package com.github.loa.queue.artemis.service.consumer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.consumer.pool.domain.PoolableQueueConsumer;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientConsumerExecutor {

    private final PoolableClientConsumerPoolFactory poolableClientConsumerPoolFactory;

    public <T> T invokeConsumer(final Queue queue, final Function<ClientConsumer, T> clientConsumerConsumer) {
        try (PoolableQueueConsumer poolableClientConsumer = claimClientConsumer(queue)) {
            return clientConsumerConsumer.apply(poolableClientConsumer.getQueueConsumer().getClientConsumer());
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
