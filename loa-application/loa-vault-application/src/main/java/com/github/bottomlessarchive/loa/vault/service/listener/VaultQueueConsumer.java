package com.github.bottomlessarchive.loa.vault.service.listener;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class VaultQueueConsumer implements Consumer<SynchronousSink<DocumentArchivingMessage>> {

    private final QueueManipulator queueManipulator;

    @Override
    public void accept(final SynchronousSink<DocumentArchivingMessage> documentArchivingMessageSynchronousSink) {
        final DocumentArchivingMessage documentArchivingMessage = queueManipulator.readMessage(
                Queue.DOCUMENT_ARCHIVING_QUEUE, DocumentArchivingMessage.class);

        log.debug("Got new archiving message: {}!", documentArchivingMessage);

        documentArchivingMessageSynchronousSink.next(documentArchivingMessage);
    }
}
