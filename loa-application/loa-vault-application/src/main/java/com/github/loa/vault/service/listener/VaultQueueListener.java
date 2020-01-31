package com.github.loa.vault.service.listener;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.loa.vault.service.VaultDocumentManager;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final VaultDocumentManager vaultDocumentManager;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);
        log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));

        Mono.just(1)
                .repeat()
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(this::readDocument)
                .flatMap(vaultDocumentManager::archiveDocument)
                .subscribe();
    }

    private Mono<DocumentArchivingContext> readDocument(final int random) {
        return Mono.fromSupplier(this::readDocumentArchivingMessage)
                .map(documentArchivingMessage -> DocumentArchivingContext.builder()
                        .type(DocumentType.valueOf(documentArchivingMessage.getType()))
                        .source(documentArchivingMessage.getSource())
                        .contentLength(documentArchivingMessage.getContentLength())
                        .content(documentArchivingMessage.getContent())
                        .build()
                );
    }

    private DocumentArchivingMessage readDocumentArchivingMessage() {
        return queueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, DocumentArchivingMessage.class);
    }
}
