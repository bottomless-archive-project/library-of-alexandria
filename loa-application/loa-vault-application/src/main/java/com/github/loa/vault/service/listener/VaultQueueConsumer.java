package com.github.loa.vault.service.listener;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.SynchronousSink;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultQueueConsumer implements Consumer<SynchronousSink<DocumentArchivingContext>> {

    private final QueueManipulator queueManipulator;

    @Override
    public void accept(final SynchronousSink<DocumentArchivingContext> documentSourceItemSynchronousSink) {
        final DocumentArchivingMessage documentArchivingMessage = (DocumentArchivingMessage)
                queueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE);

        documentSourceItemSynchronousSink.next(
                DocumentArchivingContext.builder()
                        .type(DocumentType.valueOf(documentArchivingMessage.getType()))
                        .source(documentArchivingMessage.getSource())
                        .contentLength(documentArchivingMessage.getContentLength())
                        .content(documentArchivingMessage.getContent())
                        .build()
        );
    }
}