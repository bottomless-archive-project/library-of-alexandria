package com.github.loa.queue.artemis.service.deserializer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@Service
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class DocumentArchivingMessageDeserializer implements MessageDeserializer<DocumentArchivingMessage> {

    @Override
    public DocumentArchivingMessage deserialize(final ClientMessage clientMessage) {
        final int documentContentLength = clientMessage.getBodyBuffer().readInt();

        final byte[] documentContent = new byte[documentContentLength];
        clientMessage.getBodyBuffer().readBytes(documentContent);

        return DocumentArchivingMessage.builder()
                .type(clientMessage.getBodyBuffer().readString())
                .location(clientMessage.getBodyBuffer().readString())
                .source(clientMessage.getBodyBuffer().readString())
                .content(documentContent)
                .build();
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_ARCHIVING_QUEUE;
    }
}
