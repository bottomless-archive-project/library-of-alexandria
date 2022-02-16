package com.github.bottomlessarchive.loa.indexer.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchResult;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.IndexerAccessException;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchDatabaseEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.request.IndexerRequestFactory;
import com.github.bottomlessarchive.loa.indexer.service.search.transformer.DocumentSearchEntityTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides a unified way to search for documents in the index.
 */
@Service
@RequiredArgsConstructor
public class DocumentSearchClient {

    private final ElasticsearchClient elasticsearchClient;
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
            final SearchResponse<SearchDatabaseEntity> searchResponse = elasticsearchClient.search(searchRequest,
                    SearchDatabaseEntity.class);

            return DocumentSearchResult.builder()
                    .searchHits(documentSearchEntityTransformer.transform(searchResponse.hits()))
                    .totalHitCount(
                            Optional.ofNullable(searchResponse.hits().total())
                                    .map(TotalHits::value)
                                    .orElse(0L)
                    )
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
            return elasticsearchClient.count(countRequest).count();
        } catch (final IOException e) {
            throw new IndexerAccessException("Failed to search in the indexer!", e);
        }
    }

    /**
     * Check if the document with the provided id exists in the index.
     *
     * @param documentId the id of the document to check
     * @return true if the document exists, false otherwise
     */
    public boolean isDocumentInIndex(final UUID documentId) {
        final co.elastic.clients.elasticsearch.core.ExistsRequest hasDocumentsRequest =
                indexerRequestFactory.newDocumentExistsRequest(documentId.toString());

        try {
            return elasticsearchClient.exists(hasDocumentsRequest).value();
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
            elasticsearchClient.indices().create(createIndexRequest);
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
        final ExistsRequest existsRequest = indexerRequestFactory.newIndexExistsRequest();

        try {
            final boolean exists = elasticsearchClient.indices()
                    .exists(existsRequest)
                    .value();

            if (exists) {
                return true;
            }
        } catch (final IOException e) {
            throw new IndexerAccessException("Failed to verify if search engine initialization is needed!", e);
        }

        return false;
    }
}
