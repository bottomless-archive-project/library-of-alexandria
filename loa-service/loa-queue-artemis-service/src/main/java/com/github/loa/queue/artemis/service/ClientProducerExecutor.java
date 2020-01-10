package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.pool.ClientProducerAllocator;
import com.github.loa.queue.artemis.service.pool.ClientProducerFactory;
import com.github.loa.queue.artemis.service.pool.domain.PoolableClientProducer;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import stormpot.Allocator;
import stormpot.Pool;
import stormpot.Timeout;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientProducerExecutor {

    private final Map<Queue, Pool<PoolableClientProducer>> clientProducers = new EnumMap<>(Queue.class);
    private final ClientProducerFactory clientProducerFactory;

    public void invokeProducer(final Queue queue, final Consumer<ClientProducer> clientProducerConsumer) {
        try (final PoolableClientProducer poolableClientProducer = claimClientProducer(queue)) {
            clientProducerConsumer.accept(poolableClientProducer.getClientProducer());
        }
    }

    private PoolableClientProducer claimClientProducer(final Queue queue) {
        final Pool<PoolableClientProducer> clientProducersForQueue =
                clientProducers.computeIfAbsent(queue,
                        (queue1) -> Pool.from(buildAllocatorForQueue(queue1))
                                .setSize(10)
                                .build()
                );

        try {
            return clientProducersForQueue.claim(new Timeout(120, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            throw new QueueException("Unable to acquire client producer!", e);
        }
    }

    private Allocator<PoolableClientProducer> buildAllocatorForQueue(final Queue queue) {
        return new ClientProducerAllocator(queue, clientProducerFactory);
    }
}
