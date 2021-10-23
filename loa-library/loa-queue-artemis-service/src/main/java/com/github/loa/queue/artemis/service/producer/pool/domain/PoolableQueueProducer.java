package com.github.loa.queue.artemis.service.producer.pool.domain;

import lombok.Getter;
import stormpot.BasePoolable;
import stormpot.Poolable;
import stormpot.Slot;

/**
 * This class is a wrapper for a {@link QueueProducer} that makes it {@link Poolable}.
 */
@Getter
public class PoolableQueueProducer extends BasePoolable implements AutoCloseable {

    private final QueueProducer queueProducer;

    public PoolableQueueProducer(final Slot slot, final QueueProducer queueProducer) {
        super(slot);

        this.queueProducer = queueProducer;
    }

    @Override
    public void close() {
        release();
    }
}
