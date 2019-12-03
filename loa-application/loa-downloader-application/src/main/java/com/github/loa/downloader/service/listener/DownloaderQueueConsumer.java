package com.github.loa.downloader.service.listener;

import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SynchronousSink;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloaderQueueConsumer implements Consumer<SynchronousSink<DocumentSourceItem>> {

    private final ClientConsumer clientConsumer;

    @Override
    public void accept(SynchronousSink<DocumentSourceItem> documentSourceItemSynchronousSink) {
        try {
            final DocumentSourceItem documentSourceItem = transform(clientConsumer.receive());

            documentSourceItemSynchronousSink.next(documentSourceItem);
        } catch (ActiveMQException e) {
            log.error("Connection error with the Queue Application!", e);

            documentSourceItemSynchronousSink.error(e);
        } catch (MalformedURLException e) {
            documentSourceItemSynchronousSink.error(e);
        }
    }

    private DocumentSourceItem transform(final ClientMessage clientMessage) throws MalformedURLException {
        return DocumentSourceItem.builder()
                .sourceName(clientMessage.getBodyBuffer().readString())
                .documentLocation(new URL(clientMessage.getBodyBuffer().readString()))
                .build();
    }
}
