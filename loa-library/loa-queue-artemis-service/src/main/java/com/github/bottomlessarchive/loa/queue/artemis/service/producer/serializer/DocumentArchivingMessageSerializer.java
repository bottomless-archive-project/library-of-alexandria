package com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class DocumentArchivingMessageSerializer implements MessageSerializer<DocumentArchivingMessage> {

    private final ClientSession clientSession;

    @Override
    public ClientMessage serialize(final DocumentArchivingMessage documentArchivingMessage) {
        final ClientMessage clientMessage = clientSession.createMessage(true);

        buildMessageBody(documentArchivingMessage, clientMessage);

        return clientMessage;
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_ARCHIVING_QUEUE;
    }

    private void buildMessageBody(final DocumentArchivingMessage documentArchivingMessage,
            final ClientMessage clientMessage) {
        final ActiveMQBuffer bodyBuffer = clientMessage.getBodyBuffer();

        bodyBuffer.writeString(documentArchivingMessage.id());
        bodyBuffer.writeBoolean(documentArchivingMessage.fromBeacon());
        bodyBuffer.writeString(documentArchivingMessage.type());
        bodyBuffer.writeString(documentArchivingMessage.source());
        documentArchivingMessage.sourceLocationId()
                .ifPresentOrElse(
                        value -> {
                            bodyBuffer.writeBoolean(true);
                            bodyBuffer.writeString(value);
                        },
                        () -> bodyBuffer.writeBoolean(false)
                );

        bodyBuffer.writeLong(documentArchivingMessage.contentLength());
        bodyBuffer.writeLong(documentArchivingMessage.originalContentLength());
        bodyBuffer.writeString(documentArchivingMessage.checksum());
        bodyBuffer.writeString(documentArchivingMessage.compression());
    }
}
