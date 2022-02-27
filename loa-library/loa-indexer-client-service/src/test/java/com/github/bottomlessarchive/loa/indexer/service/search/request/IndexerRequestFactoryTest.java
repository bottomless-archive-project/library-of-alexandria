package com.github.bottomlessarchive.loa.indexer.service.search.request;

import co.elastic.clients.elasticsearch._types.mapping.IndexOptions;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexerRequestFactoryTest {

    private static final String DOCUMENT_INDEX = "vault_documents";

    @Mock
    private QueryBuilderFactory queryBuilderFactory;

    @InjectMocks
    private IndexerRequestFactory underTest;

    @Test
    void testNewCountDocumentsRequest() {
        final CountRequest countRequest = underTest.newCountDocumentsRequest();

        assertThat(countRequest.index())
                .containsExactly(DOCUMENT_INDEX);
        assertThat(countRequest.query()._kind())
                .isEqualTo(Query.Kind.MatchAll);
    }

    @Test
    void testNewDocumentExistsRequest() {
        final UUID documentId = UUID.randomUUID();

        final co.elastic.clients.elasticsearch.core.ExistsRequest existsRequest = underTest.newDocumentExistsRequest(documentId);

        assertThat(existsRequest.index())
                .isEqualTo(DOCUMENT_INDEX);
        assertThat(existsRequest.id())
                .isEqualTo(documentId.toString());
        assertThat(existsRequest.storedFields())
                .containsExactly("_none_");
    }

    @Test
    void testNewIndexExistsRequest() {
        final ExistsRequest existsRequest = underTest.newIndexExistsRequest();

        assertThat(existsRequest.index())
                .containsExactly(DOCUMENT_INDEX);
    }

    @Test
    void testNewKeywordSearchRequest() {
        final SearchContext searchContext = SearchContext.builder()
                .pageNumber(100)
                .resultSize(20)
                .build();
        final Query query = mock(Query.class);
        when(queryBuilderFactory.newQueryBuilder(searchContext))
                .thenReturn(query);

        final SearchRequest existsRequest = underTest.newKeywordSearchRequest(searchContext);

        assertThat(existsRequest.index())
                .containsExactly(DOCUMENT_INDEX);
        assertThat(existsRequest.from())
                .isEqualTo(100);
        assertThat(existsRequest.size())
                .isEqualTo(20);
        assertThat(existsRequest.query())
                .isEqualTo(query);

        final Highlight highlight = existsRequest.highlight();
        assertThat(highlight.fields())
                .containsOnlyKeys("content");

        final HighlightField highlightField = highlight.fields().get("content");
        assertThat(highlightField.fragmentSize())
                .isEqualTo(350);
        assertThat(highlightField.numberOfFragments())
                .isEqualTo(3);
    }

    @Test
    void testCreateIndexRequest() {
        final CreateIndexRequest createIndexRequest = underTest.newCreateIndexRequest();

        assertThat(createIndexRequest.index())
                .isEqualTo(DOCUMENT_INDEX);

        final Map<String, Property> properties = createIndexRequest.mappings().properties();
        assertThat(properties.get("author").isText())
                .isTrue();
        assertThat(properties.get("title").isText())
                .isTrue();
        assertThat(properties.get("date").isDate())
                .isTrue();
        assertThat(properties.get("language").isKeyword())
                .isTrue();
        assertThat(properties.get("type").isKeyword())
                .isTrue();
        assertThat(properties.get("page_count").isLong())
                .isTrue();
        assertThat(properties.get("content").isText())
                .isTrue();
        assertThat(properties.get("content").text().indexOptions())
                .isEqualTo(IndexOptions.Offsets);

        final IndexSettings indexSettings = createIndexRequest.settings();
        assertThat(indexSettings.numberOfShards())
                .isEqualTo("10");
        assertThat(indexSettings.codec())
                .isEqualTo("best_compression");
        assertThat(indexSettings.refreshInterval().time())
                .isEqualTo("60s");
    }
}
