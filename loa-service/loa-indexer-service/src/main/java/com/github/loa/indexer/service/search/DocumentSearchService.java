package com.github.loa.indexer.service.search;

import com.github.loa.indexer.domain.DocumentSearchResult;
import com.github.loa.indexer.domain.IndexerAccessException;
import com.github.loa.indexer.service.search.domain.SearchContext;
import com.github.loa.indexer.service.search.request.IndexerRequestFactory;
import com.github.loa.indexer.service.search.transformer.DocumentSearchEntityTransformer;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final IndexerRequestFactory indexerRequestFactory;
    private final DocumentSearchEntityTransformer documentSearchEntityTransformer;

    public DocumentSearchResult searchDocuments(final SearchContext searchContext) {
        final SearchRequest searchRequest = indexerRequestFactory.newKeywordSearchRequest(searchContext);

        try {
            final SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            return DocumentSearchResult.builder()
                    .searchHits(documentSearchEntityTransformer.transform(searchResponse.getHits()))
                    .totalHitCount(searchResponse.getHits().getTotalHits().value)
                    .build();
        } catch (final IOException e) {
            throw new IndexerAccessException("Failed to search in the indexer!", e);
        }
    }

    public long countDocuments() {
        final CountRequest countRequest = indexerRequestFactory.newCountDocumentsRequest();

        try {
            return restHighLevelClient.count(countRequest, RequestOptions.DEFAULT).getCount();
        } catch (final IOException e) {
            throw new IndexerAccessException("Failed to search in the indexer!", e);
        }
    }
}
