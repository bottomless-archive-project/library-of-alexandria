package com.github.loa.queue.artemis.service.producer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.producer.pool.QueueProducerAllocator;
import com.github.loa.queue.artemis.service.producer.pool.QueueProducerFactory;
import com.github.loa.queue.artemis.service.producer.pool.domain.PoolableQueueProducer;
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
public class PoolableClientProducerPoolFactory {

    private final QueueProducerFactory clientProducerFactory;
    private final Map<Queue, Pool<PoolableQueueProducer>> clientProducers = new EnumMap<>(Queue.class);

    public synchronized Pool<PoolableQueueProducer> getPool(final Queue queue) {
        return clientProducers.computeIfAbsent(queue, this::createPool);
    }

    private Pool<PoolableQueueProducer> createPool(final Queue queue) {
        return Pool.from(createAllocatorForQueue(queue))
                .setSize(10)
                .setExpiration(Expiration.never())
                .build();
    }

    private Allocator<PoolableQueueProducer> createAllocatorForQueue(final Queue queue) {
        return new QueueProducerAllocator(queue, clientProducerFactory);
    }
}
