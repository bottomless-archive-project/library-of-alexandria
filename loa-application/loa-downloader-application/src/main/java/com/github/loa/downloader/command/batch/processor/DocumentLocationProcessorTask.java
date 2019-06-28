package com.github.loa.downloader.command.batch.processor;

import com.github.loa.downloader.download.service.document.DocumentDownloader;
import com.morethanheroic.taskforce.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentLocationProcessorTask implements Task<URL, URL> {

    private final DocumentDownloader documentDownloader;

    @Override
    public Optional<URL> execute(final URL url) {
        documentDownloader.downloadDocument(url);

        return Optional.of(url);
    }
}
