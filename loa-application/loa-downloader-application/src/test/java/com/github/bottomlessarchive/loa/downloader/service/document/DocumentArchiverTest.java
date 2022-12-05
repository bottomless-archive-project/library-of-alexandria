package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.compression.service.file.FileCompressionService;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.staging.service.client.StagingClient;
import io.micrometer.core.instrument.Counter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentArchiverTest {

    @Mock
    private StagingClient stagingClient;

    @Mock
    private QueueManipulator queueManipulator;

    @Mock
    private FileCompressionService fileCompressionService;

    @Mock
    private DocumentArchivingMessageFactory documentArchivingMessageFactory;

    @Mock
    private Counter archivedDocumentCount;

    @Mock
    private FileManipulatorService fileManipulatorService;

    @InjectMocks
    private DocumentArchiver documentArchiver;

    @Test
    @SneakyThrows
    void testArchiveDocument() {
        final Path documentContentLocation = mock(Path.class);
        final Path compressedDocumentContentLocation = mock(Path.class);
        final UUID documentId = UUID.randomUUID();
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(documentId)
                .contents(documentContentLocation)
                .build();
        when(fileCompressionService.compressDocument(documentContentLocation))
                .thenReturn(compressedDocumentContentLocation);
        final DocumentArchivingMessage documentArchivingMessage = DocumentArchivingMessage.builder()
                .id("id")
                .type("type")
                .source("source")
                .sourceLocationId(Optional.of("sourceLocationId"))
                .contentLength(0)
                .originalContentLength(0)
                .checksum("checksum")
                .compression("compression")
                .build();
        when(documentArchivingMessageFactory.newDocumentArchivingMessage(documentArchivingContext, compressedDocumentContentLocation))
                .thenReturn(documentArchivingMessage);

        documentArchiver.archiveDocument(documentArchivingContext);

        verify(stagingClient)
                .moveToStaging(documentId, compressedDocumentContentLocation);
        verify(queueManipulator)
                .sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, documentArchivingMessage);
        verify(fileManipulatorService)
                .delete(compressedDocumentContentLocation);
        verify(archivedDocumentCount)
                .increment();
    }
}
