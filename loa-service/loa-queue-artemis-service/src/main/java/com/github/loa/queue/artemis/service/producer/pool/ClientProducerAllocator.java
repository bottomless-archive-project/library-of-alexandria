package com.github.loa.queue.artemis.service.producer.pool;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.producer.pool.domain.PoolableClientProducer;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.QueueException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import stormpot.Allocator;
import stormpot.Slot;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ClientProducerAllocator implements Allocator<PoolableClientProducer> {

    private final Queue supportedQueue;
    private final ClientProducerFactory clientProducerFactory;

    @Override
    public PoolableClientProducer allocate(final Slot slot) {
        return new PoolableClientProducer(slot, clientProducerFactory.createProducer(supportedQueue));
    }

    @Override
    public void deallocate(final PoolableClientProducer poolableClientProducer) {
        log.info("Closing producer for queue: {}!", supportedQueue);

        try {
            poolableClientProducer.getClientProducer().close();
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to close client producer!", e);
        }
    }
}
