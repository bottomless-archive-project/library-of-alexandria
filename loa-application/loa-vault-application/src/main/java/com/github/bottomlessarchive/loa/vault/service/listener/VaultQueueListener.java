package com.github.bottomlessarchive.loa.vault.service.listener;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.staging.service.client.StagingClient;
import com.github.bottomlessarchive.loa.vault.service.VaultDocumentManager;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.vault.service.transformer.DocumentArchivingContextTransformer;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.archiving", havingValue = "true")
public class VaultQueueListener implements CommandLineRunner {

    private final StagingClient stagingClient;
    private final QueueManipulator queueManipulator;
    private final VaultDocumentManager vaultDocumentManager;
    private final DocumentArchivingContextTransformer documentArchivingContextTransformer;

    @Override
    public void run(final String... args) {
        queueManipulator.silentlyInitializeQueue(Queue.DOCUMENT_ARCHIVING_QUEUE);
        if (log.isInfoEnabled()) {
            log.info("Initialized queue processing! There are {} messages available in the archiving queue!",
                    queueManipulator.getMessageCount(Queue.DOCUMENT_ARCHIVING_QUEUE));
        }

        while (true) {
            final DocumentArchivingMessage documentArchivingMessage = queueManipulator.readMessage(
                    Queue.DOCUMENT_ARCHIVING_QUEUE, DocumentArchivingMessage.class);

            log.debug("Got new archiving message: {}!", documentArchivingMessage);

            try (InputStream documentContent = stagingClient.grabFromStaging(UUID.fromString(documentArchivingMessage.getId()))) {
                final DocumentArchivingContext documentArchivingContext = documentArchivingContextTransformer.transform(
                        documentArchivingMessage, documentContent);

                vaultDocumentManager.archiveDocument(documentArchivingContext);
            } catch (IOException e) {
                log.error("Failed to download the document's contents!");

                //TODO: Mark it as corrupt?
            }
        }
    }
}
