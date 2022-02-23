package com.github.bottomlessarchive.loa.indexer.service.indexer;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexRequestFactoryTest {

    @Mock
    private IndexDocumentFactory indexDocumentFactory;

    @InjectMocks
    private IndexRequestFactory underTest;

    @Test
    void testNewIndexRequest() {
        final UUID id = UUID.randomUUID();
        final IndexingContext indexingContext = IndexingContext.builder()
                .id(id)
                .build();
        final Map<String, Object> indexBody = Collections.emptyMap();
        when(indexDocumentFactory.buildIndexDocument(indexingContext))
                .thenReturn(indexBody);

        final IndexRequest<Map<String, Object>> result = underTest.newIndexRequest(indexingContext);

        assertThat(result.id()).isEqualTo(id.toString());
        assertThat(result.index()).isEqualTo("vault_documents");
        assertThat(result.document()).isEqualTo(indexBody);
        assertThat(result.timeout()).isNotNull();
        assertThat(result.timeout().time()).isEqualTo("30s");
    }
}