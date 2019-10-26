package com.github.loa.downloader.command.batch.task;

import com.github.loa.downloader.download.service.document.DocumentDownloader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class DocumentLocationProcessorTask {

    private final DocumentDownloader documentDownloader;

    public Mono<Void> execute(final URL url) {
        return documentDownloader.downloadDocument(url);
    }
}
