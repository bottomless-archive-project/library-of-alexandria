package com.github.loa.queue.artemis.service.consumer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.consumer.pool.ClientConsumerAllocator;
import com.github.loa.queue.artemis.service.consumer.pool.ClientConsumerFactory;
import com.github.loa.queue.artemis.service.consumer.pool.domain.PoolableClientConsumer;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import stormpot.Allocator;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientConsumerExecutor {

    private final Map<Queue, Pool<PoolableClientConsumer>> clientConsumers = new EnumMap<>(Queue.class);
    private final ClientConsumerFactory clientConsumerFactory;

    public <T> T invokeConsumer(final Queue queue, final Function<ClientConsumer, T> ClientConsumerConsumer) {
        try (final PoolableClientConsumer poolableClientConsumer = claimClientConsumer(queue)) {
            return ClientConsumerConsumer.apply(poolableClientConsumer.getClientConsumer());
        }
    }

    private PoolableClientConsumer claimClientConsumer(final Queue queue) {
        final Pool<PoolableClientConsumer> clientConsumersForQueue =
                clientConsumers.computeIfAbsent(queue,
                        (queue1) -> Pool.from(buildAllocatorForQueue(queue1))
                                .setSize(10)
                                .build()
                );

        try {
            return clientConsumersForQueue.claim(new Timeout(120, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            throw new QueueException("Unable to acquire client consumer!", e);
        }
    }

    private Allocator<PoolableClientConsumer> buildAllocatorForQueue(final Queue queue) {
        return new ClientConsumerAllocator(queue, clientConsumerFactory);
    }
}
