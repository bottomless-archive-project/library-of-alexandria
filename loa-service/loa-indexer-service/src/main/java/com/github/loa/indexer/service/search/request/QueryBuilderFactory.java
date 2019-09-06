package com.github.loa.indexer.service.search.request;

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

    public QueryBuilder newQueryBuilder(final SearchContext searchContext) {
        final BoolQueryBuilder parentQuery = QueryBuilders.boolQuery();

        initializeContentQuery(parentQuery, searchContext);
        initializeLanguageQuery(parentQuery, searchContext);

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

    private QueryBuilder newContentQuery(final boolean exactMatch, final String keyword) {
        return exactMatch ? QueryBuilders.matchPhraseQuery(SearchField.CONTENT.getName(), keyword)
                : QueryBuilders.matchQuery(SearchField.CONTENT.getName(), keyword);
    }
}
