package com.github.bottomlessarchive.loa.vault.service.archive;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentStatus;
import com.github.bottomlessarchive.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.service.domain.DocumentArchivingContext;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCreationContextFactoryTest {

    @Mock
    private VaultLocationFactory vaultLocationFactory;

    @InjectMocks
    private DocumentCreationContextFactory underTest;

    @Test
    void testNewContext() {
        final UUID id = UUID.randomUUID();
        final DocumentArchivingContext documentArchivingContext = DocumentArchivingContext.builder()
                .id(id)
                .contentLength(4)
                .originalContentLength(6L)
                .source("unknown")
                .vault("test-1")
                .type(DocumentType.PDF)
                .compression(DocumentCompression.GZIP)
                .versionNumber(123)
                .checksum("textchecksum")
                .sourceLocationId(Optional.of("locationId"))
                .build();

        when(vaultLocationFactory.getActiveVaultFileNumber())
                .thenReturn(5);

        final DocumentCreationContext result = underTest.newContext(documentArchivingContext);

        assertThat(result.id(), is(id));
        assertThat(result.fileSize(), is(6L));
        assertThat(result.source(), is("unknown"));
        assertThat(result.vault(), is("test-1"));
        assertThat(result.type(), is(DocumentType.PDF));
        assertThat(result.checksum(), is("textchecksum"));
        assertThat(result.versionNumber(), is(123));
        assertThat(result.compression(), is(DocumentCompression.GZIP));
        assertThat(result.status(), is(DocumentStatus.CREATED));
        assertThat(result.sourceLocationId().isPresent(), is(true));
        assertThat(result.sourceLocationId().get(), is("locationId"));
        assertThat(result.vaultFile(), is(5));
    }
}
