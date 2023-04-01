package com.github.bottomlessarchive.loa.downloader.service.source.beacon.service;

import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationCollector {

    private final QueueManipulator queueManipulator;
    private final DocumentLocationEvaluator documentLocationEvaluator;

    public List<DocumentLocation> collectDocumentsToProcess(final long documentCountToCollect) {
        final List<DocumentLocation> documentLocationMessages = new LinkedList<>();

        while (documentLocationMessages.size() != documentCountToCollect) {
            final Optional<DocumentLocationMessage> documentLocationMessage =
                    queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class);

            if (documentLocationMessage.isEmpty()) {
                continue;
            }

            final DocumentLocation documentLocation = DocumentLocation.builder()
                    .id(documentLocationMessage.get().getId())
                    .location(documentLocationMessage.get().getDocumentLocation())
                    .type(DocumentType.valueOf(documentLocationMessage.get().getType()))
                    .sourceName(documentLocationMessage.get().getSourceName())
                    .build();

            log.info("Processing location.");

            if (documentLocationEvaluator.shouldProcessDocumentLocation(documentLocation)) {
                documentLocationMessages.add(documentLocation);
            } else {
                log.info("Document location is a duplicate.");
            }
        }

        return documentLocationMessages;
    }
}
