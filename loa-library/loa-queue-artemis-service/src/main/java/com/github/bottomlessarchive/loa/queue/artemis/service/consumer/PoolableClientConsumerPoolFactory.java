package com.github.bottomlessarchive.loa.queue.artemis.service.consumer;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.QueueConsumerAllocator;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.QueueConsumerFactory;
import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.PoolableQueueConsumer;
import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
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

    private final QueueConsumerFactory clientConsumerFactory;
    private final Map<Queue, Pool<PoolableQueueConsumer>> clientConsumers = new EnumMap<>(Queue.class);

    public synchronized Pool<PoolableQueueConsumer> getPool(final Queue queue) {
        return clientConsumers.computeIfAbsent(queue, this::createPool);
    }

    private Pool<PoolableQueueConsumer> createPool(final Queue queue) {
        return Pool.from(createAllocatorForQueue(queue))
                .setSize(1)
                .setExpiration(Expiration.never())
                .build();
    }

    private Allocator<PoolableQueueConsumer> createAllocatorForQueue(final Queue queue) {
        return new QueueConsumerAllocator(queue, clientConsumerFactory);
    }
}
