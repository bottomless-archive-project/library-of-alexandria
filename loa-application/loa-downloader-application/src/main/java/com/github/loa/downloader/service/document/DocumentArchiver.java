package com.github.loa.downloader.service.document;

import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.loa.vault.client.service.domain.DocumentArchivingContext;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

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

            queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE,
                    DocumentArchivingMessage.builder()
                            .id(documentArchivingContext.getId().toString())
                            .type(documentArchivingContext.getType().toString())
                            .source(documentArchivingContext.getSource())
                            .contentLength(content.length)
                            .content(content)
                            .build()
            );
        } catch (final IOException e) {
            throw new RuntimeException("Failed to send document for archiving!", e);
        }

        return Mono.just(documentArchivingContext);
    }
}
