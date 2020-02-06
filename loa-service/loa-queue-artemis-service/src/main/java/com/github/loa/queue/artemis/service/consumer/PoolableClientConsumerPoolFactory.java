package com.github.loa.queue.artemis.service.consumer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.consumer.pool.ClientConsumerAllocator;
import com.github.loa.queue.artemis.service.consumer.pool.ClientConsumerFactory;
import com.github.loa.queue.artemis.service.consumer.pool.domain.PoolableClientConsumer;
import com.github.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import stormpot.Allocator;
import stormpot.Expiration;
import stormpot.Pool;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class PoolableClientConsumerPoolFactory {

    private final ClientConsumerFactory clientConsumerFactory;
    private final Map<Queue, Pool<PoolableClientConsumer>> clientConsumers = new EnumMap<>(Queue.class);

    public synchronized Pool<PoolableClientConsumer> getPool(final Queue queue) {
        return clientConsumers.computeIfAbsent(queue, this::createPool);
    }

    private Pool<PoolableClientConsumer> createPool(final Queue queue) {
        return Pool.from(createAllocatorForQueue(queue))
                .setSize(10)
                .setExpiration(Expiration.never())
                .build();
    }

    private Allocator<PoolableClientConsumer> createAllocatorForQueue(final Queue queue) {
        return new ClientConsumerAllocator(queue, clientConsumerFactory);
    }
}
