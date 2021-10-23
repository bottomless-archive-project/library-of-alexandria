package com.github.bottomlessarchive.loa.queue.artemis.service.producer.serializer;

import com.github.bottomlessarchive.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class DocumentLocationMessageSerializer implements MessageSerializer<DocumentLocationMessage> {

    private final ClientSession clientSession;

    @Override
    public ClientMessage serialize(final DocumentLocationMessage documentLocationMessage) {
        final ClientMessage clientMessage = clientSession.createMessage(true);

        buildMessageBody(documentLocationMessage, clientMessage);

        return clientMessage;
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_LOCATION_QUEUE;
    }

    private void buildMessageBody(final DocumentLocationMessage documentLocationMessage,
            final ClientMessage clientMessage) {
        final ActiveMQBuffer bodyBuffer = clientMessage.getBodyBuffer();

        bodyBuffer.writeString(documentLocationMessage.getSourceName());
        bodyBuffer.writeString(documentLocationMessage.getDocumentLocation());
    }
}
