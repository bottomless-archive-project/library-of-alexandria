package com.github.loa.indexer.service.search.request;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.stereotype.Service;

@Service
public class SearchRequestFactory {

    private static final String[] INDICES = new String[]{"vault_documents"};

    public SearchRequest newKeywordSearchRequest(final String keyword, final int pageNumber, final boolean exactMatch) {
        return new SearchRequest(INDICES, newDocumentContentQuery(keyword, pageNumber, exactMatch));
    }

    private SearchSourceBuilder newDocumentContentQuery(final String keyword, final int pageNumber,
            final boolean exactMatch) {
        return new SearchSourceBuilder()
                .from(pageNumber)
                .query(newQueryBuilder(keyword, exactMatch))
                .highlighter(newHighlightBuilder());
    }

    private QueryBuilder newQueryBuilder(final String keyword, final boolean exactMatch) {
        return exactMatch ? QueryBuilders.matchPhraseQuery("attachment.content", keyword)
                : QueryBuilders.matchQuery("attachment.content", keyword);

    }

    private HighlightBuilder newHighlightBuilder() {
        return new HighlightBuilder()
                .field("attachment.content", 500, 1)
                .order("score");
    }
}
