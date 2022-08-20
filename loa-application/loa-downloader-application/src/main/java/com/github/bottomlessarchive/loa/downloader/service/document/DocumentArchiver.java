package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.compression.service.file.FileCompressionService;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.staging.service.client.StagingClient;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * This class is responsible to archive a document. This is achieved by sending the content of the document to the
 * Staging Application and the document's metadata to the Queue Application's
 * {@link Queue#DOCUMENT_ARCHIVING_QUEUE} queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentArchiver {

    private final StagingClient stagingClient;
    private final QueueManipulator queueManipulator;
    private final FileCompressionService fileCompressionService;
    private final FileManipulatorService fileManipulatorService;
    private final DocumentArchivingMessageFactory documentArchivingMessageFactory;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    /**
     * Archiving the document that is described by the provided {@link DocumentArchivingContext}. First, the content of the document is
     * sent to the Staging Application and the document's metadata to the Queue Application.
     *
     * @param documentArchivingContext the information about the document to archive
     */
    @SneakyThrows
    public void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        final Path compressedContent = fileCompressionService.compressDocument(documentArchivingContext.getContents());

        stagingClient.moveToStaging(documentArchivingContext.getId(), compressedContent);

        final DocumentArchivingMessage documentArchivingMessage = documentArchivingMessageFactory.newDocumentArchivingMessage(
                documentArchivingContext, compressedContent);

        log.info("Sending archiving message.");

        queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, documentArchivingMessage);

        fileManipulatorService.delete(compressedContent);

        archivedDocumentCount.increment();
    }
}
