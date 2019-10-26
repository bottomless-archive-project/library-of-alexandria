package com.github.loa.downloader.command.batch.task;

import com.github.loa.downloader.download.service.document.DocumentDownloader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class DocumentLocationProcessorTask {

    private final DocumentDownloader documentDownloader;

    public void execute(final URL url) {
        System.out.println(url);

        //TODO: Enable the downloader
        //documentDownloader.downloadDocument(url);
    }
}
