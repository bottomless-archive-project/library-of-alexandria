package com.github.bottomlessarchive.loa.vault.service.listener;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.vault.service.archive.ArchivingService;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.vault.service.transformer.DocumentArchivingContextTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.archiving", havingValue = "true")
public class VaultQueueListener implements CommandLineRunner {

    private final ArchivingService archivingService;
    private final QueueManipulator queueManipulator;
    private final DocumentArchivingContextTransformer documentArchivingContextTransformer;

    @Qualifier("vaultSemaphore")
    private final Semaphore vaultSemaphore;
    @Qualifier("vaultExecutorService")
    private final ExecutorService vaultExecutorService;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);

        final Thread t = new Thread(() -> {
            while (true) {
                final DocumentArchivingMessage documentArchivingMessage = queueManipulator.readMessage(
                        Queue.DOCUMENT_ARCHIVING_QUEUE, DocumentArchivingMessage.class);

                log.debug("Got new archiving message: {}!", documentArchivingMessage);

                try {
                    vaultSemaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                vaultExecutorService.execute(() -> {
                    final DocumentArchivingContext documentArchivingContext = documentArchivingContextTransformer.transform(
                            documentArchivingMessage);

                    archivingService.archiveDocument(documentArchivingContext);

                    vaultSemaphore.release();
                });
            }
        });

        t.start();
    }
}
