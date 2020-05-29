package com.github.loa.queue.artemis.service.consumer.pool;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.consumer.pool.domain.PoolableQueueConsumer;
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
public class QueueConsumerAllocator implements Allocator<PoolableQueueConsumer> {

    private final Queue supportedQueue;
    private final QueueConsumerFactory clientConsumerFactory;

    @Override
    public PoolableQueueConsumer allocate(final Slot slot) {
        return new PoolableQueueConsumer(slot, clientConsumerFactory.createConsumer(supportedQueue));
    }

    @Override
    public void deallocate(final PoolableQueueConsumer poolable) {
        try {
            poolable.getQueueConsumer().close();
        } catch (final ActiveMQException e) {
            throw new QueueException("Unable to close queue consumer!", e);
        }
    }
}
