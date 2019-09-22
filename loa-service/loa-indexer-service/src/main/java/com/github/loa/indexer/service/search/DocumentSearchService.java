package com.github.loa.indexer.service.search;

import com.github.loa.indexer.domain.DocumentSearchResult;
import com.github.loa.indexer.domain.IndexerAccessException;
import com.github.loa.indexer.service.search.domain.SearchContext;
import com.github.loa.indexer.service.search.request.SearchRequestFactory;
import com.github.loa.indexer.service.search.transformer.DocumentSearchEntityTransformer;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final SearchRequestFactory searchRequestFactory;
    private final DocumentSearchEntityTransformer documentSearchEntityTransformer;

    public DocumentSearchResult searchDocuments(final SearchContext searchContext) {
        final SearchRequest searchRequest = searchRequestFactory.newKeywordSearchRequest(searchContext);

        try {
            final SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            return DocumentSearchResult.builder()
                    .searchHits(documentSearchEntityTransformer.transform(searchResponse.getHits()))
                    .totalHitCount(searchResponse.getHits().getTotalHits().value)
                    .build();
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to search in the indexer!", e);
        }
    }

    public long countDocuments() {
        final CountRequest countRequest = new CountRequest("vault_documents")
                .source(SearchSourceBuilder.searchSource().query(QueryBuilders.matchAllQuery()));

        try {
            return restHighLevelClient.count(countRequest, RequestOptions.DEFAULT).getCount();
        } catch (IOException e) {
            throw new IndexerAccessException("Failed to search in the indexer!", e);
        }
    }
}
