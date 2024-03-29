package com.github.bottomlessarchive.loa.queue.artemis.service.consumer.deserializer;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class DocumentArchivingMessageDeserializer implements MessageDeserializer<DocumentArchivingMessage> {

    @Override
    public DocumentArchivingMessage deserialize(final ClientMessage clientMessage) {
        final ActiveMQBuffer contentBuffer = clientMessage.getBodyBuffer();

        final String id = contentBuffer.readString();
        final boolean fromBeacon = contentBuffer.readBoolean();
        final String type = contentBuffer.readString();
        final String source = contentBuffer.readString();
        final boolean hasSourceLocationId = contentBuffer.readBoolean();
        final String sourceLocationId = hasSourceLocationId ? contentBuffer.readString() : null;

        final long contentLength = contentBuffer.readLong();
        final long originalContentLength = contentBuffer.readLong();
        final String checksum = contentBuffer.readString();
        final String compression = contentBuffer.readString();

        return DocumentArchivingMessage.builder()
                .id(id)
                .fromBeacon(fromBeacon)
                .type(type)
                .source(source)
                .sourceLocationId(Optional.ofNullable(sourceLocationId))
                .contentLength(contentLength)
                .originalContentLength(originalContentLength)
                .checksum(checksum)
                .compression(compression)
                .build();
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_ARCHIVING_QUEUE;
    }
}
