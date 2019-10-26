package com.github.loa.downloader.command.batch.task;

import com.github.loa.document.service.domain.DocumentType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Arrays;

@Service
public class DocumentLocationFilterTask {

    public boolean execute(final URL url) {
        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        final String path = url.getPath();

        return Arrays.stream(DocumentType.values()).anyMatch(documentType ->
                path.endsWith("." + documentType.getFileExtension()));
    }
}
