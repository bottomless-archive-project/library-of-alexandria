package com.github.bottomlessarchive.loa.indexer.service.indexer;

import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class IndexDocumentFactory {

    public Map<String, Object> buildIndexDocument(final IndexingContext indexingContext) {
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
