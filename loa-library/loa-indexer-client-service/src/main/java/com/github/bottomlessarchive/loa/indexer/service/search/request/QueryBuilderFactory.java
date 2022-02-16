package com.github.bottomlessarchive.loa.indexer.service.search.request;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchField;
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
    public Query newQueryBuilder(final SearchContext searchContext) {
        final BoolQuery.Builder parentQuery = QueryBuilders.bool();

        initializeContentQuery(parentQuery, searchContext);
        initializeLanguageQuery(parentQuery, searchContext);
        initializeDocumentLengthQuery(parentQuery, searchContext);
        initializeDocumentTypeQuery(parentQuery, searchContext);

        return parentQuery
                .build()
                ._toQuery();
    }

    private void initializeContentQuery(final BoolQuery.Builder parentQuery, final SearchContext searchContext) {
        final Query contentTextChildQuery = newContentQuery(searchContext.isExactMatch(),
                searchContext.getKeyword());

        parentQuery.must(contentTextChildQuery);
    }

    private void initializeLanguageQuery(final BoolQuery.Builder parentQuery, final SearchContext searchContext) {
        searchContext.getLanguage().ifPresent(language ->
                parentQuery.filter(QueryBuilders.matchPhrase()
                        .field(SearchField.LANGUAGE.getName())
                        .query(language)
                        .build()
                        ._toQuery()
                )
        );
    }

    private void initializeDocumentLengthQuery(final BoolQuery.Builder parentQuery, final SearchContext searchContext) {
        searchContext.getDocumentLength().ifPresent(documentLength ->
                parentQuery.filter(
                        QueryBuilders.range()
                                .field(SearchField.PAGE_COUNT.getName())
                                .gte(JsonData.of(documentLength.getMinimumPageCount()))
                                .lte(JsonData.of(documentLength.getMaximumPageCount()))
                                .build()
                                ._toQuery()
                )
        );
    }

    private void initializeDocumentTypeQuery(final BoolQuery.Builder parentQuery, final SearchContext searchContext) {
        if (!searchContext.getDocumentTypes().isEmpty()) {
            final BoolQuery.Builder boolQueryBuilder = QueryBuilders.bool().minimumShouldMatch("1");

            for (final DocumentType type : searchContext.getDocumentTypes()) {
                boolQueryBuilder.should(
                        QueryBuilders.term()
                                .field(SearchField.DOCUMENT_TYPE.getName())
                                .value(FieldValue.of(type.toString()))
                                .build()
                                ._toQuery()
                );
            }

            parentQuery.filter(boolQueryBuilder.build()._toQuery());
        }
    }

    private Query newContentQuery(final boolean exactMatch, final String keyword) {
        return exactMatch
                ? QueryBuilders.matchPhrase()
                .field(SearchField.CONTENT.getName())
                .query(keyword)
                .build()
                ._toQuery()
                : QueryBuilders.match()
                .field(SearchField.CONTENT.getName())
                .query(FieldValue.of(keyword))
                .build()
                ._toQuery();
    }
}
