package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.checksum.service.ChecksumProvider;
import com.github.bottomlessarchive.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.bottomlessarchive.loa.compression.service.provider.CompressionServiceProvider;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentArchiver {

    private final StagingClient stagingClient;
    private final QueueManipulator queueManipulator;
    private final ChecksumProvider checksumProvider;
    private final CompressionServiceProvider compressionServiceProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    @SneakyThrows //TODO
    public void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        archivedDocumentCount.increment();

        byte[] content = compressionServiceProvider.getCompressionService(compressionConfigurationProperties.algorithm())
                .compress(Files.newInputStream(documentArchivingContext.getContents()).readAllBytes()); //TODO: close input stream

        stagingClient.moveToStaging(documentArchivingContext.getId(), content);

        final DocumentArchivingMessage documentArchivingMessage = newDocumentArchivingMessage(content, documentArchivingContext);

        log.debug("Sending new archiving message: {}!", documentArchivingMessage);

        queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, documentArchivingMessage);
    }

    @SneakyThrows
    private DocumentArchivingMessage newDocumentArchivingMessage(final byte[] content,
            final DocumentArchivingContext documentArchivingContext) {
        return DocumentArchivingMessage.builder()
                .id(documentArchivingContext.getId().toString())
                .type(documentArchivingContext.getType().toString())
                .source(documentArchivingContext.getSource())
                .sourceLocationId(documentArchivingContext.getSourceLocationId())
                .contentLength(content.length)
                .originalContentLength(Files.size(documentArchivingContext.getContents()))
                .checksum(checksumProvider.checksum(Files.newInputStream(documentArchivingContext.getContents())))
                .compression(compressionConfigurationProperties.algorithm().toString())
                .build();
    }
}
