package com.github.loa.indexer.service.search.request;

import com.github.loa.indexer.service.search.domain.SearchContext;
import com.github.loa.indexer.service.search.domain.SearchField;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.stereotype.Service;

/**
 * Create the requests to the search engine, to query data about the indexed documents.
 */
@Service
@RequiredArgsConstructor
public class IndexerRequestFactory {

    private final QueryBuilderFactory queryBuilderFactory;

    private static final String[] INDICES = {"vault_documents"};

    /**
     * Create a request that will return the number of documents that are indexed successfully.
     *
     * @return the created request
     */
    public CountRequest newCountDocumentsRequest() {
        return new CountRequest(INDICES)
                .query(QueryBuilders.matchAllQuery());
    }

    /**
     * Create a search request based on a provided {@link SearchContext}. The searched word will be highlighted.
     *
     * @param searchContext the context of the search
     * @return the created request
     */
    public SearchRequest newKeywordSearchRequest(final SearchContext searchContext) {
        return new SearchRequest(INDICES, newDocumentContentQuery(searchContext));
    }

    private SearchSourceBuilder newDocumentContentQuery(final SearchContext searchContext) {
        final QueryBuilder queryBuilder = queryBuilderFactory.newQueryBuilder(searchContext);

        return new SearchSourceBuilder()
                .from(searchContext.getPageNumber())
                .query(queryBuilder)
                .highlighter(newHighlightBuilder());
    }


    private HighlightBuilder newHighlightBuilder() {
        return new HighlightBuilder()
                .field(SearchField.CONTENT.getName(), 500, 1)
                .order("score");
    }
}
