package com.github.loa.queue.artemis.service.serialize;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
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

        bodyBuffer.writeInt(documentArchivingMessage.getContent().length);
        bodyBuffer.writeBytes(documentArchivingMessage.getContent());
        bodyBuffer.writeString(documentArchivingMessage.getType());
        bodyBuffer.writeString(documentArchivingMessage.getSource());
    }
}
