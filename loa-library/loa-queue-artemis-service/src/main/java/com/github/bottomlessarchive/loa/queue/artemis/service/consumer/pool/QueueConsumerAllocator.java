package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool;

import com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain.PoolableQueueConsumer;
import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        poolable.getQueueConsumer().close();
    }
}
