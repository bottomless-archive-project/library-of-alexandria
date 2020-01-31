package com.github.loa.downloader.service.source.queue;

import com.github.loa.queue.service.QueueManipulator;
import com.github.loa.queue.service.domain.Queue;
import com.github.loa.queue.service.domain.message.DocumentLocationMessage;
import com.github.loa.source.domain.DocumentSourceItem;
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
@ConditionalOnProperty(value = "loa.downloader.source", havingValue = "queue", matchIfMissing = true)
public class DownloaderQueueConsumer implements Consumer<SynchronousSink<DocumentSourceItem>> {

    private final QueueManipulator queueManipulator;

    @Override
    public void accept(final SynchronousSink<DocumentSourceItem> documentSourceItemSynchronousSink) {
        final DocumentLocationMessage documentLocationMessage =
                (DocumentLocationMessage) queueManipulator.readMessage(Queue.DOCUMENT_LOCATION_QUEUE);

        try {
            documentSourceItemSynchronousSink.next(
                    DocumentSourceItem.builder()
                            .sourceName(documentLocationMessage.getSourceName())
                            .documentLocation(new URL(documentLocationMessage.getDocumentLocation()))
                            .build()
            );
        } catch (MalformedURLException e) {
            log.error("Failed to convert document location: " + documentLocationMessage.getDocumentLocation());

            documentSourceItemSynchronousSink.error(e);
        }
    }
}
