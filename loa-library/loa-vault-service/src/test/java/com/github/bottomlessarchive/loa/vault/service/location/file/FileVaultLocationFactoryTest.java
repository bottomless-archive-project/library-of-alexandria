package com.github.bottomlessarchive.loa.vault.service.location.file;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.document.service.domain.DocumentEntity;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.vault.service.location.VaultLocation;
import com.github.bottomlessarchive.loa.vault.service.location.file.configuration.FileConfigurationProperties;
import com.github.bottomlessarchive.loa.vault.service.location.file.domain.FileVaultLocation;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
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

    @InjectMocks
    private FileVaultLocationFactory underTest;

    private FileSystem fileSystem;

    @BeforeEach
    void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        testFolderPath = fileSystem.getPath("/vault/location");

        Files.createDirectories(testFolderPath);

        when(fileConfigurationProperties.path())
                .thenReturn(testFolderPath);
    }

    @AfterEach
    void teardown() throws IOException {
        fileSystem.close();
    }

    @Test
    void testGetLocationWhenNoCompression() throws IOException {
        final Path testFile = Files.createFile(testFolderPath.resolve("123e4567-e89b-12d3-a456-556642440000.epub"));

        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY, DocumentCompression.NONE);

        assertThat(result)
                .isInstanceOf(FileVaultLocation.class);
        assertThat(ReflectionTestUtils.getField(result, null, "vaultLocation"))
                .isEqualTo(testFile);
    }

    @Test
    void testGetLocationWhenCompressionAreProvided() throws IOException {
        final Path testFile = Files.createFile(testFolderPath.resolve("123e4567-e89b-12d3-a456-556642440000.epub.gz"));

        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY, DocumentCompression.GZIP);

        assertThat(result)
                .isInstanceOf(FileVaultLocation.class);
        assertThat(ReflectionTestUtils.getField(result, null, "vaultLocation"))
                .isEqualTo(testFile);
    }

    @Test
    void testGetLocationWhenDocumentEntityProvidesTheCompression() throws IOException {
        final Path testFile = Files.createFile(testFolderPath.resolve("123e4567-e89b-12d3-a456-556642440000.epub.lzma"));

        final VaultLocation result = underTest.getLocation(DOCUMENT_ENTITY);

        assertThat(result)
                .isInstanceOf(FileVaultLocation.class);
        assertThat(ReflectionTestUtils.getField(result, null, "vaultLocation"))
                .isEqualTo(testFile);
    }
}
