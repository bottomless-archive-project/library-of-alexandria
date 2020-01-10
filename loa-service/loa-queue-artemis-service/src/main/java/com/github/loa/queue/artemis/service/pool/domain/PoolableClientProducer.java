package com.github.loa.queue.artemis.service.pool.domain;

import lombok.Getter;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import stormpot.BasePoolable;
import stormpot.Slot;

@Getter
public class PoolableClientProducer extends BasePoolable {

    private final ClientProducer clientProducer;

    public PoolableClientProducer(final Slot slot, final ClientProducer clientProducer) {
        super(slot);

        this.clientProducer = clientProducer;
    }
}
