package com.github.bottomlessarchive.loa.indexer.service.indexer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.github.bottomlessarchive.loa.document.service.DocumentManipulator;
import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexerClientTest {

    @Mock
    private IndexRequestFactory indexRequestFactory;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private DocumentManipulator documentManipulator;

    @InjectMocks
    private IndexerClient underTest;

    @Test
    @SneakyThrows
    void testStatusSetToIndexedWhenContentIsNull() {
        final UUID documentId = UUID.randomUUID();
        final IndexingContext indexingContext = IndexingContext.builder()
                .id(documentId)
                .content(null)
                .build();

        underTest.indexDocument(indexingContext);

        verify(elasticsearchClient, never())
                .index(any(IndexRequest.class));
        verify(documentManipulator, only())
                .markIndexed(documentId);
    }

    @Test
    @SneakyThrows
    void testStatusSetToCorruptWhenExceptionIsThrown() {
        final UUID documentId = UUID.randomUUID();
        final IndexingContext indexingContext = IndexingContext.builder()
                .id(documentId)
                .content("some content")
                .build();
        IndexRequest<Map<String, Object>> result = new IndexRequest.Builder<Map<String, Object>>()
                .index("test")
                .document(Map.of())
                .build();
        when(indexRequestFactory.newIndexRequest(indexingContext))
                .thenReturn(result);
        when(elasticsearchClient.index(result))
                .thenThrow(ElasticsearchException.class);

        underTest.indexDocument(indexingContext);

        verify(documentManipulator, only())
                .markCorrupt(documentId);
    }

    @Test
    @SneakyThrows
    void testStatusSetToIndexedAndIndexingRequestWasSentWhenEverythingIsFine() {
        final UUID documentId = UUID.randomUUID();
        final IndexingContext indexingContext = IndexingContext.builder()
                .id(documentId)
                .content("some content")
                .build();
        IndexRequest<Map<String, Object>> result = new IndexRequest.Builder<Map<String, Object>>()
                .index("test")
                .document(Map.of())
                .build();
        when(indexRequestFactory.newIndexRequest(indexingContext))
                .thenReturn(result);

        underTest.indexDocument(indexingContext);

        verify(elasticsearchClient)
                .index(result);
        verify(documentManipulator, only())
                .markIndexed(documentId);
    }
}