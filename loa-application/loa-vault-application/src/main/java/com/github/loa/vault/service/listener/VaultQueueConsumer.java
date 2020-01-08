package com.github.loa.vault.service.listener;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.loa.vault.service.listener.domain.ArchivingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.SynchronousSink;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultQueueConsumer implements Consumer<SynchronousSink<ArchivingContext>> {

    private final QueueManipulator queueManipulator;

    @Override
    public void accept(final SynchronousSink<ArchivingContext> documentSourceItemSynchronousSink) {
        final ByteArrayOutputStream contentOutputStream = new ByteArrayOutputStream();
        final DocumentArchivingMessage documentArchivingMessage = (DocumentArchivingMessage)
                queueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, contentOutputStream);

        try {
            documentSourceItemSynchronousSink.next(
                    ArchivingContext.builder()
                            .type(DocumentType.valueOf(documentArchivingMessage.getType()))
                            .location(new URL(documentArchivingMessage.getLocation()))
                            .source(documentArchivingMessage.getSource())
                            .content(contentOutputStream)
                            .build()
            );
        } catch (MalformedURLException e) {
            log.error("Failed to convert document location.");

            documentSourceItemSynchronousSink.error(e);
        }
    }
}