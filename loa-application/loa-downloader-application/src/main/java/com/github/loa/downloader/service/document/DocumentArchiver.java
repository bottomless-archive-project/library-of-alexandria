package com.github.loa.downloader.service.document;

import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentArchivingMessage;
import com.github.loa.vault.client.service.domain.DocumentArchivingContext;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocumentArchiver {

    private final QueueManipulator queueManipulator;

    @Qualifier("archivedDocumentCount")
    private final Counter archivedDocumentCount;

    public void archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        try (final InputStream documentContent = new FileInputStream(documentArchivingContext.getContents().toFile())) {
            archivedDocumentCount.increment();

            queueManipulator.sendMessage(Queue.DOCUMENT_ARCHIVING_QUEUE,
                    DocumentArchivingMessage.builder()
                            .type(documentArchivingContext.getType().toString())
                            .source(documentArchivingContext.getSource())
                            .content(documentContent.readAllBytes())
                            .build()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to send document for archiving!", e);
        }
    }
}
