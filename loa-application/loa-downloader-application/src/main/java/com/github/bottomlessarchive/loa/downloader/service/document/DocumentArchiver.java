package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.compression.service.file.FileCompressionService;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
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

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentArchiver {

    private final StagingClient stagingClient;
    private final QueueManipulator queueManipulator;
    private final FileCompressionService fileCompressionService;
    private final DocumentArchivingMessageFactory documentArchivingMessageFactory;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    @SneakyThrows
    public void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        archivedDocumentCount.increment();

        final Path compressedContent = fileCompressionService.compressDocument(documentArchivingContext.getContents());

        stagingClient.moveToStaging(documentArchivingContext.getId(), compressedContent);

        final DocumentArchivingMessage documentArchivingMessage = documentArchivingMessageFactory.newDocumentArchivingMessage(
                documentArchivingContext, compressedContent);

        log.debug("Sending new archiving message: {}!", documentArchivingMessage);

        queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, documentArchivingMessage);

        Files.delete(compressedContent);
    }

}
