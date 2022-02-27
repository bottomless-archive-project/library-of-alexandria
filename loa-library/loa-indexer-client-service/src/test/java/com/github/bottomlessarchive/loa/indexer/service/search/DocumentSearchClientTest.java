package com.github.bottomlessarchive.loa.indexer.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchResult;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.IndexerAccessException;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchDatabaseEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.request.IndexerRequestFactory;
import com.github.bottomlessarchive.loa.indexer.service.search.transformer.DocumentSearchEntityTransformer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentSearchClientTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private IndexerRequestFactory indexerRequestFactory;

    @Mock
    private DocumentSearchEntityTransformer documentSearchEntityTransformer;

    @InjectMocks
    private DocumentSearchClient underTest;

    @Test
    @SneakyThrows
    void testSearchDocumentsWhenExceptionIsThrown() {
        final SearchContext searchContext = SearchContext.builder()
                .build();
        final SearchRequest searchRequest = mock(SearchRequest.class);
        when(indexerRequestFactory.newKeywordSearchRequest(searchContext))
                .thenReturn(searchRequest);
        when(elasticsearchClient.search(searchRequest, SearchDatabaseEntity.class))
                .thenThrow(new IOException());

        assertThrows(IndexerAccessException.class, () -> underTest.searchDocuments(searchContext));
    }

    @Test
    @SneakyThrows
    void testSearchDocumentsWhenSearchIsSuccessful() {
        final SearchContext searchContext = SearchContext.builder()
                .build();
        final SearchRequest searchRequest = mock(SearchRequest.class);
        when(indexerRequestFactory.newKeywordSearchRequest(searchContext))
                .thenReturn(searchRequest);
        final HitsMetadata<SearchDatabaseEntity> hitsMetadata = HitsMetadata.of(metadataBuilder ->
                metadataBuilder.hits(Collections.emptyList())
                        .total(builder -> builder
                                .relation(TotalHitsRelation.Eq)
                                .value(222)
                        )
        );
        final SearchResponse<SearchDatabaseEntity> searchResponse = new SearchResponse.Builder<SearchDatabaseEntity>()
                .took(123)
                .timedOut(false)
                .shards(builder -> builder.failed(0)
                        .successful(10)
                        .total(10)
                )
                .hits(hitsMetadata)
                .build();
        when(elasticsearchClient.search(searchRequest, SearchDatabaseEntity.class))
                .thenReturn(searchResponse);
        final List<DocumentSearchEntity> searchEntities = Collections.emptyList();
        when(documentSearchEntityTransformer.transform(hitsMetadata))
                .thenReturn(searchEntities);

        final DocumentSearchResult result = underTest.searchDocuments(searchContext);

        assertThat(result.getTotalHitCount())
                .isEqualTo(222);
        assertThat(result.getSearchHits())
                .isSameAs(searchEntities);
    }

    @Test
    @SneakyThrows
    void testSearchDocumentsWhenTotalHitsIsNull() {
        final SearchContext searchContext = SearchContext.builder()
                .build();
        final SearchRequest searchRequest = mock(SearchRequest.class);
        when(indexerRequestFactory.newKeywordSearchRequest(searchContext))
                .thenReturn(searchRequest);
        final HitsMetadata<SearchDatabaseEntity> hitsMetadata = HitsMetadata.of(metadataBuilder ->
                metadataBuilder.hits(Collections.emptyList())
        );
        final SearchResponse<SearchDatabaseEntity> searchResponse = new SearchResponse.Builder<SearchDatabaseEntity>()
                .took(123)
                .timedOut(false)
                .shards(builder -> builder.failed(0)
                        .successful(10)
                        .total(10)
                )
                .hits(hitsMetadata)
                .build();
        when(elasticsearchClient.search(searchRequest, SearchDatabaseEntity.class))
                .thenReturn(searchResponse);
        final List<DocumentSearchEntity> searchEntities = Collections.emptyList();
        when(documentSearchEntityTransformer.transform(hitsMetadata))
                .thenReturn(searchEntities);

        final DocumentSearchResult result = underTest.searchDocuments(searchContext);

        assertThat(result.getTotalHitCount())
                .isEqualTo(0);
        assertThat(result.getSearchHits())
                .isSameAs(searchEntities);
    }

    @Test
    @SneakyThrows
    void testCountDocumentsWhenExceptionIsThrown() {
        final CountRequest countRequest = CountRequest.of(builder -> builder);
        when(indexerRequestFactory.newCountDocumentsRequest())
                .thenReturn(countRequest);
        when(elasticsearchClient.count(countRequest))
                .thenThrow(new IOException());

        assertThrows(IndexerAccessException.class, () -> underTest.countDocuments());
    }

    @Test
    @SneakyThrows
    void testCountDocumentsWhenCountIsSuccessful() {
        final CountRequest countRequest = CountRequest.of(builder -> builder);
        when(indexerRequestFactory.newCountDocumentsRequest())
                .thenReturn(countRequest);
        final CountResponse countResponse = CountResponse.of(countBuilder ->
                countBuilder.count(222)
                        .shards(builder -> builder.failed(0)
                                .successful(10)
                                .total(10)
                        )
        );
        when(elasticsearchClient.count(countRequest))
                .thenReturn(countResponse);

        final long result = underTest.countDocuments();

        assertThat(result).isEqualTo(222);
    }

    @Test
    @SneakyThrows
    void testIsDocumentInIndexWhenExceptionIsThrown() {
        final UUID documentId = UUID.randomUUID();
        final co.elastic.clients.elasticsearch.core.ExistsRequest hasDocumentsRequest =
                co.elastic.clients.elasticsearch.core.ExistsRequest.of(builder -> builder);
        when(indexerRequestFactory.newDocumentExistsRequest(documentId))
                .thenReturn(hasDocumentsRequest);
        when(elasticsearchClient.exists(hasDocumentsRequest))
                .thenThrow(new IOException());

        assertThrows(IndexerAccessException.class, () -> underTest.isDocumentInIndex(documentId));
    }

    @Test
    @SneakyThrows
    void testIsDocumentInIndexWhenExistsIsSuccessful() {
        final UUID documentId = UUID.randomUUID();
        final co.elastic.clients.elasticsearch.core.ExistsRequest hasDocumentsRequest =
                co.elastic.clients.elasticsearch.core.ExistsRequest.of(builder ->
                        builder.id(documentId.toString())
                                .index("test")
                );
        when(indexerRequestFactory.newDocumentExistsRequest(documentId))
                .thenReturn(hasDocumentsRequest);
        when(elasticsearchClient.exists(hasDocumentsRequest))
                .thenReturn(new BooleanResponse(true));

        final boolean result = underTest.isDocumentInIndex(documentId);

        assertThat(result).isTrue();
    }
}
