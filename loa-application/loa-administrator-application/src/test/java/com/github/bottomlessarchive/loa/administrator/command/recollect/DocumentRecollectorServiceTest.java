package com.github.bottomlessarchive.loa.administrator.command.recollect;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRecollectorServiceTest {

    @Mock
    private SourceLocationRecrawlerService sourceLocationRecrawlerService;

    @InjectMocks
    private DocumentRecollectorService underTest;

    @Test
    void testRecollectCorruptDocumentOnlyDownloadsTheFirstOne() {
        DocumentEntity documentEntity = DocumentEntity.builder()
                .sourceLocations(
                        Set.of(
                                "http://abc1.com/",
                                "http://abc2.com/",
                                "http://abc3.com/"
                        )
                )
                .build();
        when(sourceLocationRecrawlerService.recrawlSourceLocation(any(), eq(documentEntity)))
                .thenReturn(Mono.empty(), Mono.just(documentEntity));

        Flux<DocumentEntity> result = underTest.recollectCorruptDocument(documentEntity);

        StepVerifier.create(result)
                .consumeNextWith(localDocumentEntity -> Assertions.assertEquals(documentEntity, localDocumentEntity))
                .verifyComplete();
        verify(sourceLocationRecrawlerService, times(2)).recrawlSourceLocation(any(), any());
    }
}
