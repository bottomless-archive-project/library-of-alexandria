package com.github.loa.queue.artemis.service.deserializer;

import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentLocationMessage;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.stereotype.Service;

@Service
public class DocumentLocationMessageDeserializer implements MessageDeserializer<DocumentLocationMessage> {

    public DocumentLocationMessage deserialize(final ClientMessage clientMessage) {
        return DocumentLocationMessage.builder()
                .sourceName(clientMessage.getBodyBuffer().readString())
                .documentLocation(clientMessage.getBodyBuffer().readString())
                .build();
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_LOCATION_QUEUE;
    }
}
