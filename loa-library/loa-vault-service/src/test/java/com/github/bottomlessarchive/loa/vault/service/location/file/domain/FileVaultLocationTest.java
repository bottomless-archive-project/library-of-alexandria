package com.github.bottomlessarchive.loa.vault.service.location.file.domain;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.vault.domain.exception.StorageAccessException;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FileVaultLocationTest {

    private FileSystem fileSystem;

    @BeforeEach
    public void setup() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    @SneakyThrows
    public void teardown() {
        fileSystem.close();
    }

    @Test
    @SneakyThrows
    void testDownload() {
        final byte[] testFileContent = {12, 34, 56, 78};
        final Path testFilePath = fileSystem.getPath("/test");

        Files.copy(new ByteArrayInputStream(testFileContent), testFilePath);

        final FileVaultLocation underTest = new FileVaultLocation(testFilePath, DocumentCompression.NONE);

        final byte[] result = underTest.download().readAllBytes();

        assertThat(result, equalTo(testFileContent));
    }

    @Test
    void testDownloadWhenExceptionThrown() {
        final FileVaultLocation underTest = new FileVaultLocation(fileSystem.getPath("/test"), DocumentCompression.NONE);

        // The message should be the same as used in the VaultClientService class!
        assertThrows(StorageAccessException.class, underTest::download, "Unable to get document content on a vault location!");
    }
}
