package com.github.bottomlessarchive.loa.downloader.service.source.queue;

import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationEvaluator;
import com.github.bottomlessarchive.loa.downloader.service.document.DocumentLocationProcessorWrapper;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueMessageHandler {

    private final DocumentLocationProcessorWrapper documentLocationProcessorWrapper;
    private final DocumentLocationEvaluator documentLocationEvaluator;

    public void handleMessage(final DocumentLocationMessage documentLocationMessage) {
        final DocumentLocation documentLocation = buildDocumentLocationFromMessage(documentLocationMessage);

        log.info("Processing location.");

        if (documentLocationEvaluator.shouldProcessDocumentLocation(documentLocation)) {
            documentLocationProcessorWrapper.processDocumentLocation(documentLocation);
        } else {
            log.info("Document location is a duplicate.");
        }
    }

    @SneakyThrows
    private DocumentLocation buildDocumentLocationFromMessage(final DocumentLocationMessage documentLocationMessage) {
        return DocumentLocation.builder()
                .id(documentLocationMessage.getId())
                .type(DocumentType.valueOf(documentLocationMessage.getType()))
                .location(new URL(documentLocationMessage.getDocumentLocation()))
                .sourceName(documentLocationMessage.getSourceName())
                .build();
    }
}
