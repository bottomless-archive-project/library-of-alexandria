package com.github.loa.queue.artemis.service.producer;

import com.github.loa.queue.artemis.service.producer.pool.ClientProducerAllocator;
import com.github.loa.queue.artemis.service.producer.pool.ClientProducerFactory;
import com.github.loa.queue.artemis.service.producer.pool.domain.PoolableClientProducer;
import com.github.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stormpot.Allocator;
import stormpot.Expiration;
import stormpot.Pool;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PoolableClientProducerPoolFactory {

    private final ClientProducerFactory clientProducerFactory;
    private final Map<Queue, Pool<PoolableClientProducer>> clientProducers = new EnumMap<>(Queue.class);

    public synchronized Pool<PoolableClientProducer> getPool(final Queue queue) {
        return clientProducers.computeIfAbsent(queue, this::createPool);
    }

    private Pool<PoolableClientProducer> createPool(final Queue queue) {
        return Pool.from(createAllocatorForQueue(queue))
                .setSize(10)
                .setExpiration(Expiration.never())
                .build();
    }

    private Allocator<PoolableClientProducer> createAllocatorForQueue(final Queue queue) {
        return new ClientProducerAllocator(queue, clientProducerFactory);
    }
}
