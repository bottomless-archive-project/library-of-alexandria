package com.github.bottomlessarchive.loa.downloader.service.source.queue;

import com.github.bottomlessarchive.loa.location.domain.link.UrlLink;
import com.github.bottomlessarchive.loa.location.service.id.factory.DocumentLocationIdFactory;
import com.github.bottomlessarchive.loa.queue.service.QueueManipulator;
import com.github.bottomlessarchive.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocation;
import com.github.bottomlessarchive.loa.queue.service.domain.Queue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "queue")
public class DownloaderQueueConsumer implements Consumer<SynchronousSink<DocumentLocation>> {

    private final QueueManipulator queueManipulator;
    private final DocumentLocationIdFactory documentLocationIdFactory;

    @Override
    public void accept(final SynchronousSink<DocumentLocation> documentSourceItemSynchronousSink) {
        final DocumentLocationMessage documentLocationMessage =
                queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class);

        try {
            final URL documentLocationURL = new URL(documentLocationMessage.getDocumentLocation());

            documentSourceItemSynchronousSink.next(
                    DocumentLocation.builder()
                            .id(documentLocationIdFactory.newDocumentLocationId(documentLocationURL))
                            .location(
                                    UrlLink.builder()
                                            .url(documentLocationURL)
                                            .build()
                            )
                            .sourceName(documentLocationMessage.getSourceName())
                            .build()
            );
        } catch (final MalformedURLException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to convert document location: {}", documentLocationMessage.getDocumentLocation());
            }

            documentSourceItemSynchronousSink.error(e);
        }
    }
}
