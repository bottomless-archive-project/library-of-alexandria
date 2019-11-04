package com.github.loa.downloader.command;

import com.github.loa.downloader.download.service.document.DocumentDownloader;
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
    public void receive(final Message<URL> message) {
        final URL documentLocation = message.getPayload();

        Mono.just(documentLocation)
                .flatMap(documentDownloader::downloadDocument)
                .block();
    }
}
