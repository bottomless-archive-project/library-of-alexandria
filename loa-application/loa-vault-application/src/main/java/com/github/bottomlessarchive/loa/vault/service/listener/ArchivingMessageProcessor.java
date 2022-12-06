package com.github.bottomlessarchive.loa.vault.service.listener;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.vault.service.archive.ArchivingService;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.vault.service.transformer.DocumentArchivingContextTransformer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivingMessageProcessor {

    private final ArchivingService archivingService;
    private final QueueManipulator queueManipulator;
    private final ConfigurableApplicationContext applicationContext;
    private final DocumentArchivingContextTransformer documentArchivingContextTransformer;

    @Qualifier("vaultSemaphore")
    private final Semaphore vaultSemaphore;

    @Qualifier("vaultExecutorService")
    private final ExecutorService vaultExecutorService;

    /**
     * Process messages from the {@link Queue#DOCUMENT_ARCHIVING_QUEUE} as long as the application's {@link ApplicationContext} is not
     * closed.
     */
    @SneakyThrows
    public void processMessages() {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);

        while (applicationContext.isActive()) {
            queueManipulator.readMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, DocumentArchivingMessage.class)
                    .ifPresent(this::processMessage);
        }
    }

    @SneakyThrows
    private void processMessage(final DocumentArchivingMessage documentArchivingMessage) {
        log.debug("Got new archiving message: {}!", documentArchivingMessage);

        vaultSemaphore.acquire();

        vaultExecutorService.execute(() -> {
            final DocumentArchivingContext documentArchivingContext = documentArchivingContextTransformer.transform(
                    documentArchivingMessage);

            archivingService.archiveDocument(documentArchivingContext);

            vaultSemaphore.release();
        });
    }
}
