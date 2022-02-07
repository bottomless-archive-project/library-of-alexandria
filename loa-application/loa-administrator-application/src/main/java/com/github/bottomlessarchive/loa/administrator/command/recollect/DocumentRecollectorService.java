package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty("recollect-corrupt-documents")
public class DocumentRecollectorService {

    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final SourceLocationRecrawlerService sourceLocationRecrawlerService;

    public void recollectCorruptDocument(final DocumentEntity documentEntity) {
        log.info("Recollecting document entity: {}.", documentEntity);

        documentEntity.getSourceLocations().stream()
                .flatMap(id -> documentLocationEntityFactory.getDocumentLocation(id).stream())
                .map(this::convertToURL)
                .forEach(sourceLocation -> sourceLocationRecrawlerService.recrawlSourceLocation(sourceLocation, documentEntity));
    }

    private URL convertToURL(final DocumentLocation sourceLocation) {
        try {
            return new URL(sourceLocation.getUrl());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Illegal URL: " + sourceLocation + "!", e);
        }
    }
}
