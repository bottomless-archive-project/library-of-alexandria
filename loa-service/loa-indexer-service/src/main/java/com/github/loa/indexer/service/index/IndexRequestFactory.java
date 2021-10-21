package com.github.loa.indexer.service.index;

import com.github.loa.document.service.DocumentManipulator;
import com.github.loa.parser.domain.DocumentMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.core.TimeValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    private final DocumentManipulator documentManipulator;

    public Mono<IndexRequest> newIndexRequest(final DocumentMetadata documentMetadata) {
        if (documentMetadata.getContent() == null) {
            if (log.isInfoEnabled()) {
                log.info("Marking {} as indexed, even if it has no parsable content!", documentMetadata.getId());
            }

            return documentManipulator.markIndexed(documentMetadata.getId())
                    .then(Mono.empty());
        }

        return Mono.just(
                new IndexRequest("vault_documents")
                        .id(documentMetadata.getId().toString())
                        .source(buildSourceContent(documentMetadata))
                        .timeout(TimeValue.timeValueMinutes(30))
        );
    }

    private Map<String, Object> buildSourceContent(final DocumentMetadata documentMetadata) {
        final Map<String, Object> sourceContent = new HashMap<>();

        sourceContent.put("content", documentMetadata.getContent().trim());

        if (documentMetadata.getTitle() != null && !documentMetadata.getTitle().isBlank()) {
            sourceContent.put("title", documentMetadata.getTitle().trim());
        }

        if (documentMetadata.getAuthor() != null && !documentMetadata.getAuthor().isBlank()) {
            sourceContent.put("author", documentMetadata.getAuthor().trim());
        }

        if (documentMetadata.getDate() != null && !documentMetadata.getDate().isBlank()) {
            sourceContent.put("date", documentMetadata.getDate().trim());
        }

        if (documentMetadata.getLanguage() != null && !documentMetadata.getLanguage().isBlank()) {
            sourceContent.put("language", documentMetadata.getLanguage().trim());
        }

        sourceContent.put("page_count", documentMetadata.getPageCount());
        sourceContent.put("type", documentMetadata.getType());

        return sourceContent;
    }
}
