package com.github.loa.queue.artemis.service.consumer.deserializer;

import com.github.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

public interface MessageDeserializer<T> {

    T deserialize(ClientMessage clientMessage);

    Queue supports();
}
