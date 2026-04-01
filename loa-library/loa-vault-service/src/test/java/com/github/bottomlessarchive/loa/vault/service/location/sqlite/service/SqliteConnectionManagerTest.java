package com.github.bottomlessarchive.loa.vault.service.location.sqlite.service;

import com.github.bottomlessarchive.loa.vault.service.location.sqlite.configuration.SqliteConfigurationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SqliteConnectionManagerTest {

    @TempDir
    private Path tempDir;

    private SqliteConnectionManager underTest;

    @BeforeEach
    void setup() {
        underTest = createManager(100000);
        underTest.initialize();
    }

    @AfterEach
    void teardown() {
        underTest.close();
    }

    @Test
    void testInitializationCreatesFirstVaultFile() {
        assertThat(underTest.getActiveVaultFileNumber()).isEqualTo(1);
        assertThat(tempDir.resolve("vault-000001.db")).exists();
    }

    @Test
    void testInsertAndReadDocument() throws Exception {
        final byte[] content = {1, 2, 3, 4, 5};

        underTest.insertDocument("doc-001", new ByteArrayInputStream(content));

        try (InputStream result = underTest.readDocument(1, "doc-001")) {
            assertThat(result.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void testDocumentExistsReturnsTrueWhenPresent() {
        final byte[] content = {1, 2, 3};
        underTest.insertDocument("doc-exists", new ByteArrayInputStream(content));

        assertThat(underTest.documentExists(1, "doc-exists")).isTrue();
    }

    @Test
    void testDocumentExistsReturnsFalseWhenAbsent() {
        assertThat(underTest.documentExists(1, "nonexistent")).isFalse();
    }

    @Test
    void testDeleteDocument() {
        final byte[] content = {1, 2, 3};
        underTest.insertDocument("doc-delete", new ByteArrayInputStream(content));

        underTest.deleteDocument(1, "doc-delete");

        assertThat(underTest.documentExists(1, "doc-delete")).isFalse();
    }

    @Test
    void testRotationCreatesNewFile() {
        underTest.close();
        underTest = createManager(3);
        underTest.initialize();

        underTest.insertDocument("doc-1", new ByteArrayInputStream(new byte[]{1}));
        underTest.insertDocument("doc-2", new ByteArrayInputStream(new byte[]{2}));
        underTest.insertDocument("doc-3", new ByteArrayInputStream(new byte[]{3}));

        assertThat(underTest.getActiveVaultFileNumber()).isEqualTo(2);
        assertThat(tempDir.resolve("vault-000002.db")).exists();
    }

    @Test
    void testReadFromPreviousFileAfterRotation() throws Exception {
        underTest.close();
        underTest = createManager(2);
        underTest.initialize();

        underTest.insertDocument("doc-a", new ByteArrayInputStream(new byte[]{10, 20}));
        underTest.insertDocument("doc-b", new ByteArrayInputStream(new byte[]{30, 40}));

        assertThat(underTest.getActiveVaultFileNumber()).isEqualTo(2);

        try (InputStream result = underTest.readDocument(1, "doc-a")) {
            assertThat(result.readAllBytes()).isEqualTo(new byte[]{10, 20});
        }
    }

    @Test
    void testNewInstancePicksUpExistingState() {
        final byte[] content = {99, 88, 77};
        underTest.insertDocument("doc-persist", new ByteArrayInputStream(content));
        underTest.close();

        final SqliteConnectionManager newManager = createManager(100000);
        newManager.initialize();

        try {
            assertThat(newManager.getActiveVaultFileNumber()).isEqualTo(1);
            assertThat(newManager.documentExists(1, "doc-persist")).isTrue();

            try (InputStream result = newManager.readDocument(1, "doc-persist")) {
                assertThat(result.readAllBytes()).isEqualTo(content);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            newManager.close();
        }
    }

    @Test
    void testGetAvailableSpace() {
        assertThat(underTest.getAvailableSpace()).isGreaterThan(0);
    }

    private SqliteConnectionManager createManager(final int batchSize) {
        final SqliteConfigurationProperties config = new SqliteConfigurationProperties(tempDir, batchSize);
        return new SqliteConnectionManager(config);
    }
}
