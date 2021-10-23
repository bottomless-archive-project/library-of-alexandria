package com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer;

import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

public interface MessageSerializer<T> {

    ClientMessage serialize(T message);

    Queue supports();
}
