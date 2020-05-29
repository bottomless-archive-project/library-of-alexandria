package com.github.loa.queue.artemis.service.producer.pool.domain;

import lombok.Builder;
import lombok.Getter;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;

@Getter
@Builder
public class QueueProducer implements AutoCloseable {

    private final ClientSession clientSession;
    private final ClientProducer clientProducer;

    @Override
    public void close() throws ActiveMQException {
        clientProducer.close();
        clientSession.close();
    }
}
