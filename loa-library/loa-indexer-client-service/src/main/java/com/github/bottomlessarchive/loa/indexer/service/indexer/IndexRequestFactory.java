package com.github.bottomlessarchive.loa.indexer.service.indexer;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    public IndexRequest<Map<String, Object>> newIndexRequest(final IndexingContext indexingContext) {
        return new IndexRequest.Builder<Map<String, Object>>()
                .id(indexingContext.getId().toString())
                .index("vault_documents")
                .document(buildSourceContent(indexingContext))
                //.timeout() //TODO: Figure this out!
                .build();
    }

    private Map<String, Object> buildSourceContent(final IndexingContext indexingContext) {
        final Map<String, Object> sourceContent = new HashMap<>();

        sourceContent.put("content", indexingContext.getContent().trim());

        if (indexingContext.getTitle() != null && !indexingContext.getTitle().isBlank()) {
            sourceContent.put("title", indexingContext.getTitle().trim());
        }

        if (indexingContext.getAuthor() != null && !indexingContext.getAuthor().isBlank()) {
            sourceContent.put("author", indexingContext.getAuthor().trim());
        }

        if (indexingContext.getDate() != null && !indexingContext.getDate().isBlank()) {
            sourceContent.put("date", indexingContext.getDate().trim());
        }

        if (indexingContext.getLanguage() != null && !indexingContext.getLanguage().isBlank()) {
            sourceContent.put("language", indexingContext.getLanguage().trim());
        }

        sourceContent.put("page_count", indexingContext.getPageCount());
        sourceContent.put("type", indexingContext.getType());

        return sourceContent;
    }
}
