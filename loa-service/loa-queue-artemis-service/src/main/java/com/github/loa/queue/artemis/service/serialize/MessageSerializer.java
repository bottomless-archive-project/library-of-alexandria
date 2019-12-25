package com.github.loa.queue.artemis.service.serialize;

import com.github.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

public interface MessageSerializer<T> {

    ClientMessage serialize(T message);

    Queue supports();
}
