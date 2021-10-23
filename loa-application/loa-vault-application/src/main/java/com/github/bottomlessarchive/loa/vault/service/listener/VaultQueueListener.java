package com.github.bottomlessarchive.loa.vault.service.listener;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.transformer.DocumentArchivingContextTransformer;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.archiving", havingValue = "true")
public class VaultQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final VaultDocumentManager vaultDocumentManager;
    private final VaultQueueConsumer vaultQueueConsumer;
    private final DocumentArchivingContextTransformer documentArchivingContextTransformer;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);
        if (log.isInfoEnabled()) {
            log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                    queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));
        }

        Flux.generate(vaultQueueConsumer)
                .publishOn(Schedulers.boundedElastic())
                .map(documentArchivingContextTransformer::transform)
                .flatMap(vaultDocumentManager::archiveDocument)
                .subscribe();
    }
}
