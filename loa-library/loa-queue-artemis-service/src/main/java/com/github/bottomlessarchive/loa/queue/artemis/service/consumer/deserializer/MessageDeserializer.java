package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer;

import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import org.apache.activemq.artemis.api.core.client.ClientMessage;

public interface MessageDeserializer<T> {

    T deserialize(ClientMessage clientMessage);

    Queue supports();
}
