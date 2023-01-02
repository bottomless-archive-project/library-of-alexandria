package com.github.bottomlessarchive.loa.vault.service.location.file;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.file.configuration.FileConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.location.file.domain.FileVaultLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileVaultLocationFactoryTest {

    private static final DocumentEntity DOCUMENT_ENTITY = DocumentEntity.builder()
            .id(UUID.fromString("123e4567-e89b-12d3-a456-556642440000"))
            .type(DocumentType.EPUB)
            .compression(DocumentCompression.LZMA)
            .build();

    @Mock
    private FileConfigurationProperties fileConfigurationProperties;

    @Mock
    private Path testFolderPath;

    @Mock
    private Path testFilePath;

    @InjectMocks
    private FileVaultLocationFactory underTest;

    @BeforeEach
    void setup() {
        when(fileConfigurationProperties.path())
                .thenReturn(testFolderPath);
    }

    @Test
    void testGetLocationWhenNoCompression() {
        when(testFolderPath.resolve("123e4567-e89b-12d3-a456-556642440000.epub"))
                .thenReturn(testFilePath);

        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY, DocumentCompression.NONE);

        assertThat(result)
                .isInstanceOf(FileVaultLocation.class);
        assertThat(ReflectionTestUtils.getField(result, null, "vaultLocation"))
                .isSameAs(testFilePath);
    }

    @Test
    void testGetLocationWhenCompressionAreProvided() {
        when(testFolderPath.resolve("123e4567-e89b-12d3-a456-556642440000.epub.gz"))
                .thenReturn(testFilePath);

        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY, DocumentCompression.GZIP);

        assertThat(result)
                .isInstanceOf(FileVaultLocation.class);
        assertThat(ReflectionTestUtils.getField(result, null, "vaultLocation"))
                .isSameAs(testFilePath);
    }

    @Test
    void testGetLocationWhenDocumentEntityProvidesTheCompression() {
        when(testFolderPath.resolve("123e4567-e89b-12d3-a456-556642440000.epub.lzma"))
                .thenReturn(testFilePath);

        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY);

        assertThat(result)
                .isInstanceOf(FileVaultLocation.class);
        assertThat(ReflectionTestUtils.getField(result, null, "vaultLocation"))
                .isSameAs(testFilePath);
    }
}
