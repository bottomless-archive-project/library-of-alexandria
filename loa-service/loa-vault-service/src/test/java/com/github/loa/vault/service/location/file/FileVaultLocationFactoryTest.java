package com.github.loa.vault.service.location.file;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.vault.configuration.location.file.FileConfigurationProperties;
import com.github.loa.vault.service.location.VaultLocation;
import com.github.loa.vault.service.location.file.domain.FileVaultLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileVaultLocationFactoryTest {

    private static final String TEST_FILE_PATH = "testpath";

    private static final DocumentEntity DOCUMENT_ENTITY = DocumentEntity.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-556642440000"))
            .type(DocumentType.EPUB)
            .compression(DocumentCompression.LZMA)
            .build();

    @Mock
    private FileConfigurationProperties fileConfigurationProperties;

    @InjectMocks
    private FileVaultLocationFactory underTest;

    @BeforeEach
    void setup() {
        when(fileConfigurationProperties.getPath())
                .thenReturn(TEST_FILE_PATH);
    }

    @Test
    void testGetLocationWhenNoCompression() {
        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY, DocumentCompression.NONE);

        assertThat(result.file().getPath(), is("testpath" + File.separator
                + "123e4567-e89b-12d3-a456-556642440000.epub"));
    }

    @Test
    void testGetLocationWhenCompressionAreProvided() {
        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY, DocumentCompression.GZIP);

        assertThat(result.file().getPath(), is("testpath" + File.separator
                + "123e4567-e89b-12d3-a456-556642440000.epub.gz"));
    }

    @Test
    void testGetLocationWhenDocumentEntityProvidesTheCompression() {
        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY);

        assertThat(result.file().getPath(), is("testpath" + File.separator
                + "123e4567-e89b-12d3-a456-556642440000.epub.lzma"));
    }
}
