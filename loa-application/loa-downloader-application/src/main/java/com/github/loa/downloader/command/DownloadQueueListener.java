package com.github.loa.downloader.command;

import com.github.loa.downloader.download.service.document.DocumentDownloader;
import com.github.loa.source.domain.DocumentSourceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadQueueListener {

    private final DocumentDownloader documentDownloader;

    @JmsListener(destination = "loa.downloader", concurrency = "10-10")
    public void receive(final Message<DocumentSourceItem> message) {
        final DocumentSourceItem documentLocation = message.getPayload();

        Mono.just(documentLocation)
                .flatMap(documentDownloader::downloadDocument)
                .block();
    }
}
