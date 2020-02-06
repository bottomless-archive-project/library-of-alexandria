package com.github.loa.queue.artemis.service.consumer.deserializer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
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

        final String type = contentBuffer.readString();
        final String source = contentBuffer.readString();

        final int contentLength = contentBuffer.readInt();
        final byte[] content = new byte[contentLength];
        clientMessage.getBodyBuffer().readBytes(content);

        return DocumentArchivingMessage.builder()
                .type(type)
                .source(source)
                .contentLength(contentLength)
                .content(content)
                .build();
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_ARCHIVING_QUEUE;
    }
}
