package com.github.bottomlessarchive.loa.indexer.service.indexer;

import com.github.bottomlessarchive.loa.indexer.service.indexer.domain.IndexingContext;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IndexDocumentFactoryTest {

    private IndexDocumentFactory underTest;

    @BeforeEach
    void setup() {
        underTest = new IndexDocumentFactory();
    }

    @Test
    void testThatOptionalParametersWereNotAddedWhenTheyAreNull() {
        final Map<String, Object> result = underTest.buildIndexDocument(
                IndexingContext.builder()
                        .content("")
                        .build()
        );

        assertThat(result)
                .doesNotContainKeys("title", "author", "date", "language");
    }

    @Test
    void testThatOptionalParametersWereNotAddedWhenTheyAreBlank() {
        final Map<String, Object> result = underTest.buildIndexDocument(
                IndexingContext.builder()
                        .content("")
                        .title("  ")
                        .author("   ")
                        .date("   ")
                        .language("  ")
                        .build()
        );

        assertThat(result)
                .doesNotContainKeys("title", "author", "date", "language");
    }

    @Test
    void testParametersAreSanitizedAndAddedCorrectly() {
        final Map<String, Object> result = underTest.buildIndexDocument(
                IndexingContext.builder()
                        .content(" test-content ")
                        .title(" test-title ")
                        .author(" test-author ")
                        .date(" test-date ")
                        .language(" test-language ")
                        .pageCount(10)
                        .type(DocumentType.DOC)
                        .build()
        );

        assertThat(result)
                .containsEntry("content", "test-content")
                .containsEntry("title", "test-title")
                .containsEntry("author", "test-author")
                .containsEntry("date", "test-date")
                .containsEntry("language", "test-language")
                .containsEntry("page_count", 10)
                .containsEntry("type", DocumentType.DOC);
    }
}
