package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class DocumentArchivingMessageDeserializer implements MessageDeserializer<DocumentArchivingMessage> {

    @Override
    public DocumentArchivingMessage deserialize(final ClientMessage clientMessage) {
        final ActiveMQBuffer contentBuffer = clientMessage.getBodyBuffer();

        final String id = contentBuffer.readString();
        final String type = contentBuffer.readString();
        final String source = contentBuffer.readString();
        final boolean hasSourceLocationId = contentBuffer.readBoolean();
        final String sourceLocationId = hasSourceLocationId ? contentBuffer.readString() : null;

        final long contentLength = contentBuffer.readLong();
        final String checksum = contentBuffer.readString();

        return DocumentArchivingMessage.builder()
                .id(id)
                .type(type)
                .source(source)
                .sourceLocationId(sourceLocationId)
                .contentLength(contentLength)
                .checksum(checksum)
                .build();
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_ARCHIVING_QUEUE;
    }
}
