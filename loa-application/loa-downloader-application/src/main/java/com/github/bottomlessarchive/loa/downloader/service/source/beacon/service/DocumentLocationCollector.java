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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentLocationCollector {

    private final QueueManipulator queueManipulator;
    private final DocumentLocationEvaluator documentLocationEvaluator;

    public List<DocumentLocation> collectDocumentsToProcess(final int documentCountToCollect) {
        final List<DocumentLocation> documentLocationMessages = new ArrayList<>(documentCountToCollect);

        while (documentLocationMessages.size() != documentCountToCollect) {
            final DocumentLocationMessage documentLocationMessage =
                    queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class);

            try {
                final DocumentLocation documentLocation = DocumentLocation.builder()
                        .id(documentLocationMessage.getId())
                        .location(new URL(documentLocationMessage.getDocumentLocation()))
                        .type(DocumentType.valueOf(documentLocationMessage.getType()))
                        .sourceName(documentLocationMessage.getSourceName())
                        .build();

                log.info("Processing location.");

                if (documentLocationEvaluator.shouldProcessDocumentLocation(documentLocation)) {
                    documentLocationMessages.add(documentLocation);
                } else {
                    log.info("Document location is a duplicate.");
                }
            } catch (MalformedURLException e) {
                log.error("Incorrect document location! This shouldn't happen!", e);
            }
        }

        return documentLocationMessages;
    }
}
