package com.github.loa.queue.artemis.service.producer.serializer;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

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

        bodyBuffer.writeString(documentArchivingMessage.getType());
        bodyBuffer.writeString(documentArchivingMessage.getSource());
        bodyBuffer.writeInt(documentArchivingMessage.getContentLength());

        final InputStream contentInputStream = documentArchivingMessage.getContent();

        try {
            while (contentInputStream.available() > 0) {
                final byte[] part = contentInputStream.readNBytes(1024);

                bodyBuffer.writeBytes(part);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
