package com.github.loa.vault.service.listener;

import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.vault.service.VaultDocumentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultQueueListener implements CommandLineRunner {

    private final QueueManipulator queueManipulator;
    private final VaultQueueConsumer vaultQueueConsumer;
    private final VaultDocumentManager vaultDocumentManager;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);

        log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));

        Flux.generate(vaultQueueConsumer)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(vaultDocumentManager::archiveDocument)
                .subscribe();
    }
}
