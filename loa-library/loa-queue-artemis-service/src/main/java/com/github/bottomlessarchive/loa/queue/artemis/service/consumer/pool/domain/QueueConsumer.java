package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.pool.domain;

import com.github.bottomlessarchive.loa.queue.service.domain.QueueException;
import lombok.Builder;
import lombok.Getter;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientSession;

@Getter
@Builder
public class QueueConsumer implements AutoCloseable {

    private final ClientSession clientSession;
    private final ClientConsumer clientConsumer;

    @Override
    public void close() {
        try {
            clientConsumer.close();
            clientSession.close();
        } catch (final ActiveMQException e) {
            throw new QueueException("Failed to close queue consumer!", e);
        }
    }
}
