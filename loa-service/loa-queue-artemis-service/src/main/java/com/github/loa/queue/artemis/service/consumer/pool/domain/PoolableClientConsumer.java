package com.github.loa.queue.artemis.service.consumer.pool.domain;

import lombok.Getter;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import stormpot.BasePoolable;
import stormpot.Poolable;
import stormpot.Slot;

/**
 * This class is a wrapper for a {@link ClientConsumer} that makes it {@link Poolable}.
 */
@Getter
public class PoolableClientConsumer extends BasePoolable implements AutoCloseable {

    private final ClientConsumer clientConsumer;

    public PoolableClientConsumer(final Slot slot, final ClientConsumer clientConsumer) {
        super(slot);

        this.clientConsumer = clientConsumer;
    }

    @Override
    public void close() {
        release();
    }
}
