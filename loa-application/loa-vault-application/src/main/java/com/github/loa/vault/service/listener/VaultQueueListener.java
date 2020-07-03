package com.github.loa.vault.service.listener;

import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.loa.vault.service.transformer.DocumentArchivingContextTransformer;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.archiving", havingValue = "true")
public class VaultQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentArchivingContextTransformer documentArchivingContextTransformer;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);
        log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));

        Mono.just(1)
                .repeat()
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .map(random -> readDocumentArchivingMessage())
                .map(documentArchivingContextTransformer::transform)
                .flatMap(vaultDocumentManager::archiveDocument)
                .subscribe();
    }

    private DocumentArchivingMessage readDocumentArchivingMessage() {
        return queueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, DocumentArchivingMessage.class);
    }
}
