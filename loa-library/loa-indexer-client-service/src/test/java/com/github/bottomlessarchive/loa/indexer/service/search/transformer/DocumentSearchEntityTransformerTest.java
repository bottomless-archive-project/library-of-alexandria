package com.github.bottomlessarchive.loa.indexer.service.search.transformer;

import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchEntity;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.SearchDatabaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentSearchEntityTransformerTest {

    private DocumentSearchEntityTransformer underTest;

    @BeforeEach
    void setup() {
        underTest = new DocumentSearchEntityTransformer();
    }

    @Test
    void testTransform() {
        final SearchDatabaseEntity firstSearchDatabaseEntity = new SearchDatabaseEntity();
        firstSearchDatabaseEntity.setTitle("first-title");
        firstSearchDatabaseEntity.setAuthor("first-author");
        firstSearchDatabaseEntity.setLanguage("first-language");
        firstSearchDatabaseEntity.setPageCount(10);
        firstSearchDatabaseEntity.setType(DocumentType.PDF);

        final SearchDatabaseEntity secondSearchDatabaseEntity = new SearchDatabaseEntity();
        secondSearchDatabaseEntity.setTitle("second-title");
        secondSearchDatabaseEntity.setAuthor("second-author");
        secondSearchDatabaseEntity.setLanguage("second-language");
        secondSearchDatabaseEntity.setPageCount(20);
        secondSearchDatabaseEntity.setType(DocumentType.DOC);

        final HitsMetadata<SearchDatabaseEntity> hitsMetadata = HitsMetadata.of(builder ->
                builder.hits(
                        List.of(
                                Hit.of(hitBuilder -> hitBuilder.source(firstSearchDatabaseEntity)
                                        .id("first-id")
                                        .index("test")
                                        .highlight(
                                                Map.of("content",
                                                        List.of(
                                                                "first-fragment-1 • \r\n •",
                                                                "first-fragment-2",
                                                                "first-fragment-3"
                                                        )
                                                )
                                        )
                                ),
                                Hit.of(hitBuilder -> hitBuilder.source(secondSearchDatabaseEntity)
                                        .id("second-id")
                                        .index("test")
                                        .highlight(
                                                Map.of("content",
                                                        List.of(
                                                                "second-fragment-1 • \r\n •",
                                                                "second-fragment-2",
                                                                "second-fragment-3"
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        final List<DocumentSearchEntity> result = underTest.transform(hitsMetadata);

        assertThat(result)
                .hasSize(2);

        final DocumentSearchEntity firstSearchEntity = result.get(0);
        assertThat(firstSearchEntity.getTitle())
                .isEqualTo("first-title");
        assertThat(firstSearchEntity.getAuthor())
                .isEqualTo("first-author");
        assertThat(firstSearchEntity.getLanguage())
                .isEqualTo("first-language");
        assertThat(firstSearchEntity.getPageCount())
                .isEqualTo(10);
        assertThat(firstSearchEntity.getType())
                .isEqualTo(DocumentType.PDF);
        assertThat(firstSearchEntity.getDescription())
                .containsExactly(
                        "first-fragment-1",
                        "first-fragment-2",
                        "first-fragment-3"
                );

        final DocumentSearchEntity secondSearchEntity = result.get(1);
        assertThat(secondSearchEntity.getTitle())
                .isEqualTo("second-title");
        assertThat(secondSearchEntity.getAuthor())
                .isEqualTo("second-author");
        assertThat(secondSearchEntity.getLanguage())
                .isEqualTo("second-language");
        assertThat(secondSearchEntity.getPageCount())
                .isEqualTo(20);
        assertThat(secondSearchEntity.getType())
                .isEqualTo(DocumentType.DOC);
        assertThat(secondSearchEntity.getDescription())
                .containsExactly(
                        "second-fragment-1",
                        "second-fragment-2",
                        "second-fragment-3"
                );
    }
}
