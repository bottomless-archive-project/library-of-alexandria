package com.github.bottomlessarchive.loa.downloader.service.document;

import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.downloader.service.document.domain.exception.ArchivingException;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentArchiver {

    private final QueueManipulator queueManipulator;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    public Mono<DocumentArchivingContext> archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        try (InputStream documentContent = Files.newInputStream(documentArchivingContext.getContents())) {
            archivedDocumentCount.increment();

            final byte[] content = documentContent.readAllBytes();

            final DocumentArchivingMessage documentArchivingMessage = newDocumentArchivingMessage(
                    documentArchivingContext, content);

            log.debug("Sending new archiving message: {}!", documentArchivingMessage);

            queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE, documentArchivingMessage);
        } catch (final IOException e) {
            throw new ArchivingException("Failed to send document for archiving!", e);
        }

        return Mono.just(documentArchivingContext);
    }

    public DocumentArchivingMessage newDocumentArchivingMessage(final DocumentArchivingContext documentArchivingContext,
            final byte[] content) {
        return DocumentArchivingMessage.builder()
                .id(documentArchivingContext.getId().toString())
                .type(documentArchivingContext.getType().toString())
                .source(documentArchivingContext.getSource())
                .sourceLocationId(documentArchivingContext.getSourceLocationId())
                .contentLength(content.length)
                .content(content)
                .build();
    }
}
