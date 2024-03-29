package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.location.service.factory.DocumentLocationEntityFactory;
import com.github.bottomlessarchive.loa.location.service.factory.domain.DocumentLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class DocumentRecollectorServiceTest {

    @Mock
    private SourceLocationRecrawlerService sourceLocationRecrawlerService;

    @Mock
    private DocumentLocationEntityFactory documentLocationEntityFactory;

    @InjectMocks
    private DocumentRecollectorService underTest;

    @Test
    void testRecollectCorruptDocumentOnlyDownloadsTheFirstOne() {
        final String firstLocationId = "123";
        final String secondLocationId = "456";
        final String thirdLocationId = "789";
        final DocumentEntity documentEntity = DocumentEntity.builder()
                .sourceLocations(Set.of(firstLocationId, secondLocationId, thirdLocationId))
                .build();
        Mockito.when(documentLocationEntityFactory.getDocumentLocation(any()))
                .thenReturn(
                        Optional.of(
                                DocumentLocation.builder()
                                        .id("first")
                                        .url("http://abc1.com")
                                        .build()
                        )
                );
        Mockito.doThrow(new RuntimeException())
                .doNothing()
                .when(sourceLocationRecrawlerService).recrawlSourceLocation(any(), any());

        underTest.recollectCorruptDocument(documentEntity);

        Mockito.verify(sourceLocationRecrawlerService, Mockito.times(2)).recrawlSourceLocation(any(), eq(documentEntity));
    }
}
