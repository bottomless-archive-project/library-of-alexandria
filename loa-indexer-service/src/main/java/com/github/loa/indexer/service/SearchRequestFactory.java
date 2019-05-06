package com.github.loa.indexer.service;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.stereotype.Service;

@Service
public class SearchRequestFactory {

    private static final String[] INDICES = new String[]{"vault_documents"};

    public SearchRequest newKeywordSearchRequest(final String keyword, final int pageNumber) {
        return new SearchRequest(INDICES, newDocumentContentQuery(keyword, pageNumber));
    }

    private SearchSourceBuilder newDocumentContentQuery(final String keyword, final int pageNumber) {
        return new SearchSourceBuilder()
                .from(pageNumber)
                .query(newQueryBuilder(keyword))
                .highlighter(newHighlightBuilder());
    }

    private QueryBuilder newQueryBuilder(final String keyword) {
        return QueryBuilders.matchQuery("attachment.content", keyword);
    }

    private HighlightBuilder newHighlightBuilder() {
        return new HighlightBuilder()
                .field("attachment.content", 500, 1)
                .order("score");
    }
}
