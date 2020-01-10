package com.github.loa.queue.artemis.service.pool.domain;

import lombok.Getter;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import stormpot.BasePoolable;
import stormpot.Poolable;
import stormpot.Slot;

/**
 * This class is a wrapper for a {@link ClientProducer} that makes it {@link Poolable}.
 */
@Getter
public class PoolableClientProducer extends BasePoolable implements AutoCloseable {

    private final ClientProducer clientProducer;

    public PoolableClientProducer(final Slot slot, final ClientProducer clientProducer) {
        super(slot);

        this.clientProducer = clientProducer;
    }

    @Override
    public void close() {
        release();
    }
}
