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
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Provides a unified way to search for documents in the index.
 */
@Service
@RequiredArgsConstructor
public class DocumentSearchService {

    private final RestHighLevelClient restHighLevelClient;
    private final IndexerRequestFactory indexerRequestFactory;
    private final DocumentSearchEntityTransformer documentSearchEntityTransformer;

    /**
     * Search for documents in the index.
     *
     * @param searchContext the parameters of the search
     * @return the end result of the search
     */
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

    /**
     * Count the indexed documents.
     *
     * @return the number of the indexed documents
     */
    public long countDocuments() {
        final CountRequest countRequest = indexerRequestFactory.newCountDocumentsRequest();

        try {
            return restHighLevelClient.count(countRequest, RequestOptions.DEFAULT).getCount();
        } catch (final IOException e) {
            throw new IndexerAccessException("Failed to search in the indexer!", e);
        }
    }

    /**
     * Initialize the search engine for document indexing.
     */
    public void initializeSearchEngine() {
        final CreateIndexRequest createIndexRequest = indexerRequestFactory.newCreateIndexRequest();

        try {
            restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        } catch (final IOException e) {
            throw new IndexerAccessException("Failed to initialize the search engine!", e);
        }
    }

    /**
     * Check if the search engine was initialized.
     *
     * @return true if the search engine is initialized, false otherwise
     */
    public boolean isSearchEngineInitialized() {
        final GetIndexRequest getIndexRequest = indexerRequestFactory.newIndexExistsRequest();

        try {
            final boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

            if (exists) {
                return true;
            }
        } catch (final IOException e) {
            throw new IndexerAccessException("Failed to verify if search engine initialization is needed!", e);
        }

        return false;
    }
}
