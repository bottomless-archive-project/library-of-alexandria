package com.github.loa.queue.artemis.service;

import com.github.loa.queue.artemis.configuration.QueueServerConfiguration;
import com.github.loa.queue.artemis.service.domain.ArtemisQueueMessage;
import com.github.loa.queue.service.QueueMessageFactory;
import com.github.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.loa.queue.service.domain.message.QueueMessage;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(QueueServerConfiguration.class)
public class ArtemisQueueMessageFactory implements QueueMessageFactory {

    private final ClientSession clientSession;

    public QueueMessage newDocumentLocationQueueMessage(final DocumentLocationMessage documentLocationMessage) {
        final ClientMessage clientMessage = clientSession.createMessage(true);

        clientMessage.getBodyBuffer().writeString(documentLocationMessage.getSourceName());
        clientMessage.getBodyBuffer().writeString(documentLocationMessage.getDocumentLocation());

        return ArtemisQueueMessage.builder()
                .clientMessage(clientMessage)
                .build();
    }
}
