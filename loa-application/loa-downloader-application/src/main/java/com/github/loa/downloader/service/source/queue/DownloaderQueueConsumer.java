package com.github.loa.downloader.service.source.queue;

import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentLocationMessage;
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

    @Override
    public void accept(final SynchronousSink<DocumentLocation> documentSourceItemSynchronousSink) {
        final DocumentLocationMessage documentLocationMessage =
                queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE, DocumentLocationMessage.class);

        try {
            documentSourceItemSynchronousSink.next(
                    DocumentLocation.builder()
                            .sourceName(documentLocationMessage.getSourceName())
                            .location(new URL(documentLocationMessage.getDocumentLocation()))
                            .build()
            );
        } catch (final MalformedURLException e) {
            log.error("Failed to convert document location: {}", documentLocationMessage.getDocumentLocation());

            documentSourceItemSynchronousSink.error(e);
        }
    }
}
