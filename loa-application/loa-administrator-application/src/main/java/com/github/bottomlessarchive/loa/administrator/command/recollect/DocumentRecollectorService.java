package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty("recollect-corrupt-documents")
public class DocumentRecollectorService {

    private final DocumentLocationEntityFactory documentLocationEntityFactory;
    private final SourceLocationRecrawlerService sourceLocationRecrawlerService;

    public void recollectCorruptDocument(final DocumentEntity documentEntity) {
        log.info("Recollecting document entity: {}.", documentEntity);

        final List<DocumentLocation> sourceLocations = documentEntity.getSourceLocations().stream()
                .flatMap(id -> documentLocationEntityFactory.getDocumentLocation(id).stream())
                .toList();

        for (final DocumentLocation location : sourceLocations) {
            try {
                sourceLocationRecrawlerService.recrawlSourceLocation(location, documentEntity);

                // Downloading only until one of the recrawls is successful.
                break;
            } catch (final Exception e) {
                log.debug("Failed to recrawl from location: " + location + "!", e);
            }
        }
    }
}
