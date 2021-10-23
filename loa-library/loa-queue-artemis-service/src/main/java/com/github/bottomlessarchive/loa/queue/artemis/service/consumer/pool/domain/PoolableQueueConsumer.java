package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain;

import lombok.Getter;
import stormpot.BasePoolable;
import stormpot.Poolable;
import stormpot.Slot;

/**
 * This class is a wrapper for a {@link QueueConsumer} that makes it {@link Poolable}.
 */
@Getter
public class PoolableQueueConsumer extends BasePoolable implements AutoCloseable {

    private final QueueConsumer queueConsumer;

    public PoolableQueueConsumer(final Slot slot, final QueueConsumer queueConsumer) {
        super(slot);

        this.queueConsumer = queueConsumer;
    }

    @Override
    public void close() {
        release();
    }
}
