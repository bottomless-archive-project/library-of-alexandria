package com.github.bottomlessarchive.loa.vault.service.listener;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.vault.service.archive.ArchivingService;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.vault.service.transformer.DocumentArchivingContextTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.archiving", havingValue = "true")
public class VaultQueueListener implements CommandLineRunner {

    private final ArchivingService archivingService;
    private final QueueManipulator queueManipulator;
    private final DocumentArchivingContextTransformer documentArchivingContextTransformer;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);

        if (log.isInfoEnabled()) {
            log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                    queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));
        }

        //TODO: Make this parallel!
        while (true) {
            final DocumentArchivingMessage documentArchivingMessage = queueManipulator.readMessage(
                    Queue.DOCUMENT_ARCHIVING_QUEUE, DocumentArchivingMessage.class);

            log.debug("Got new archiving message: {}!", documentArchivingMessage);

            final DocumentArchivingContext documentArchivingContext = documentArchivingContextTransformer.transform(
                    documentArchivingMessage);

            archivingService.archiveDocument(documentArchivingContext);
        }
    }
}
