package com.github.bottomlessarchive.loa.queue.artemis.service.producer.pool;

import com.github.bottomlessarchive.loa.queue.artemis.service.producer.pool.domain.PoolableQueueProducer;
import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import stormpot.Allocator;
import stormpot.Slot;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class QueueProducerAllocator implements Allocator<PoolableQueueProducer> {

    private final Queue supportedQueue;
    private final QueueProducerFactory queueProducerFactory;

    @Override
    public PoolableQueueProducer allocate(final Slot slot) {
        return new PoolableQueueProducer(slot, queueProducerFactory.createProducer(supportedQueue));
    }

    @Override
    public void deallocate(final PoolableQueueProducer poolableClientProducer) {
        log.info("Closing producer for queue: {}!", supportedQueue);

        try {
            poolableClientProducer.getQueueProducer().close();
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to close client producer!", e);
        }
    }
}
