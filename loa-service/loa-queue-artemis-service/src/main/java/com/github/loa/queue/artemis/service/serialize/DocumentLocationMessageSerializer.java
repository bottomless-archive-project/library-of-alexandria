package com.github.loa.queue.artemis.service.serialize;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentLocationMessage;
import lombok.RequiredArgsConstructor;
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
    public ClientMessage serialize(final DocumentLocationMessage message) {
        final ClientMessage clientMessage = clientSession.createMessage(true);

        clientMessage.getBodyBuffer().writeString(message.getSourceName());
        clientMessage.getBodyBuffer().writeString(message.getDocumentLocation());

        return clientMessage;
    }

    @Override
    public Queue supports() {
        return Queue.DOCUMENT_LOCATION_QUEUE;
    }
}
