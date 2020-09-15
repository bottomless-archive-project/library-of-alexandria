package com.github.loa.indexer.service.search.request;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.indexer.service.search.domain.SearchContext;
import com.github.loa.indexer.service.search.domain.SearchField;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

/**
 * This class is responsible to build the search query for a search request to the search engine.
 */
@Service
public class QueryBuilderFactory {

    /**
     * Create a search query based on the provided {@link SearchContext}.
     *
     * @param searchContext the context to base the query creation on
     * @return the query
     */
    public QueryBuilder newQueryBuilder(final SearchContext searchContext) {
        final BoolQueryBuilder parentQuery = QueryBuilders.boolQuery();

        initializeContentQuery(parentQuery, searchContext);
        initializeLanguageQuery(parentQuery, searchContext);
        initializeDocumentLengthQuery(parentQuery, searchContext);
        initializeDocumentTypeQuery(parentQuery, searchContext);

        return parentQuery;
    }

    private void initializeContentQuery(final BoolQueryBuilder parentQuery, final SearchContext searchContext) {
        final QueryBuilder contentTextChildQuery = newContentQuery(searchContext.isExactMatch(),
                searchContext.getKeyword());

        parentQuery.must(contentTextChildQuery);
    }

    private void initializeLanguageQuery(final BoolQueryBuilder parentQuery, final SearchContext searchContext) {
        searchContext.getLanguage().ifPresent(language ->
                parentQuery.filter(QueryBuilders.matchPhraseQuery(SearchField.LANGUAGE.getName(), language)));
    }

    private void initializeDocumentLengthQuery(final BoolQueryBuilder parentQuery, final SearchContext searchContext) {
        searchContext.getDocumentLength().ifPresent(documentLength ->
                parentQuery.filter(
                        QueryBuilders.rangeQuery(SearchField.PAGE_COUNT.getName())
                                .gte(documentLength.getMinimumPageCount())
                                .lte(documentLength.getMaximumPageCount())
                )
        );
    }

    private void initializeDocumentTypeQuery(final BoolQueryBuilder parentQuery, final SearchContext searchContext) {
        if (!searchContext.getDocumentTypes().isEmpty()) {
            final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);

            for (final DocumentType type : searchContext.getDocumentTypes()) {
                boolQueryBuilder.should(QueryBuilders.termQuery(SearchField.DOCUMENT_TYPE.getName(), type.toString()));
            }
            parentQuery.filter(boolQueryBuilder);
        }
    }

    private QueryBuilder newContentQuery(final boolean exactMatch, final String keyword) {
        return exactMatch ? QueryBuilders.matchPhraseQuery(SearchField.CONTENT.getName(), keyword)
                : QueryBuilders.matchQuery(SearchField.CONTENT.getName(), keyword);
    }
}
