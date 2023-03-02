package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.bottomlessarchive.loa.indexer.service.search.domain.DocumentSearchEntity;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.web.view.document.response.SearchDocumentEntityResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.atIndex;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDocumentEntityResponseTransformerTest {

    @Mock
    private DocumentEntityFactory documentEntityFactory;

    @InjectMocks
    private SearchDocumentEntityResponseTransformer underTest;

    @Test
    void testTransformWhenDataIsPresentInDatabase() {
        final UUID idAsUUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3");

        final DocumentSearchEntity documentSearchEntity = DocumentSearchEntity.builder()
                .id(idAsUUID.toString())
                .title("Test document")
                .author("Test author")
                .description(
                        List.of(
                                "Test description 1",
                                "Test description 2",
                                "Test description 3"
                        )
                )
                .pageCount(123)
                .type(DocumentType.DOC)
                .build();

        when(documentEntityFactory.getDocumentEntity(idAsUUID))
                .thenReturn(
                        Optional.of(
                                DocumentEntity.builder()
                                        .id(idAsUUID)
                                        .vault("vault-1")
                                        .source("test-source")
                                        .downloadDate(Instant.ofEpochSecond(1000))
                                        .sourceLocations(Set.of("123", "456", "789"))
                                        .build()
                        )
                );

        final List<SearchDocumentEntityResponse> result = underTest.transform(List.of(documentSearchEntity));

        assertThat(result)
                .hasSize(1)
                .satisfies(response -> {
                    assertThat(response.getId())
                            .isEqualTo(idAsUUID.toString());
                    assertThat(response.getTitle())
                            .isEqualTo("Test document");
                    assertThat(response.getAuthor())
                            .isEqualTo("Test author");
                    assertThat(response.getDescription())
                            .hasSize(3)
                            .satisfies(description -> assertThat(description).isEqualTo("Test description 1"), atIndex(0))
                            .satisfies(description -> assertThat(description).isEqualTo("Test description 2"), atIndex(1))
                            .satisfies(description -> assertThat(description).isEqualTo("Test description 3"), atIndex(2));
                    assertThat(response.getPageCount())
                            .isEqualTo(123);
                    assertThat(response.getType())
                            .isEqualTo(DocumentType.DOC);
                    assertThat(response.getVault())
                            .isEqualTo("vault-1");
                    assertThat(response.getSource())
                            .isEqualTo("test-source");
                    assertThat(response.getDownloadDate())
                            .isEqualTo(Instant.ofEpochSecond(1000));
                    assertThat(response.getSourceLocations())
                            .containsExactlyInAnyOrder("123", "456", "789");
                }, atIndex(0));
    }

    @Test
    void testTransformWhenDataIsNotPresentInDatabase() {
        final UUID idAsUUID = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3");

        final DocumentSearchEntity documentSearchEntity = DocumentSearchEntity.builder()
                .id(idAsUUID.toString())
                .build();

        when(documentEntityFactory.getDocumentEntity(idAsUUID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.transform(List.of(documentSearchEntity)))
                .hasMessage("500 INTERNAL_SERVER_ERROR \"Unable to find the document with id: e58ed763-928c-4155-bee9-fdbaaadc15f3 "
                        + "in the database!\"")
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
