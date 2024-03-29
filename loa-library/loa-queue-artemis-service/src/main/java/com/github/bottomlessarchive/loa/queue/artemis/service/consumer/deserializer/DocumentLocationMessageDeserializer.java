package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class DocumentLocationMessageDeserializer implements MessageDeserializer<DocumentLocationMessage> {

    @Override
    public DocumentLocationMessage deserialize(final ClientMessage clientMessage) {
        final ActiveMQBuffer bodyBuffer = clientMessage.getBodyBuffer();

        return DocumentLocationMessage.builder()
                .id(bodyBuffer.readString())
                .type(bodyBuffer.readString())
                .sourceName(bodyBuffer.readString())
                .documentLocation(bodyBuffer.readString())
                .build();
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_LOCATION_QUEUE;
    }
}
