package com.github.loa.queue.artemis.service.consumer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.consumer.pool.domain.PoolableClientConsumer;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientConsumerExecutor {

    private final PoolableClientConsumerPoolFactory poolableClientConsumerPoolFactory;

    public void invokeConsumer(final Queue queue, final Consumer<ClientConsumer> clientConsumerConsumer) {
        try (final PoolableClientConsumer poolableClientConsumer = claimClientConsumer(queue)) {
            clientConsumerConsumer.accept(poolableClientConsumer.getClientConsumer());
        }
    }

    private PoolableClientConsumer claimClientConsumer(final Queue queue) {
        final Pool<PoolableClientConsumer> clientConsumersForQueue = poolableClientConsumerPoolFactory.getPool(queue);

        try {
            return clientConsumersForQueue.claim(new Timeout(Integer.MAX_VALUE, TimeUnit.DAYS));
        } catch (final InterruptedException e) {
            throw new QueueException("Unable to acquire client consumer!", e);
        }
    }
}
