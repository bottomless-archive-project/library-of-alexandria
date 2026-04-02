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
        assertThat(tempDir.resolve("vault-000001.db")).exists();
    }

    @Test
    void testAssignVaultFileNumber() {
        final int fileNumber = underTest.assignVaultFileNumber("doc-001");

        assertThat(fileNumber).isEqualTo(1);
    }

    @Test
    void testInsertAndReadDocument() throws Exception {
        final byte[] content = {1, 2, 3, 4, 5};
        final int fileNumber = underTest.assignVaultFileNumber("doc-001");

        underTest.insertDocument(fileNumber, "doc-001", new ByteArrayInputStream(content));

        try (InputStream result = underTest.readDocument(fileNumber, "doc-001")) {
            assertThat(result.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    void testDocumentExistsReturnsTrueWhenPresent() {
        final byte[] content = {1, 2, 3};
        final int fileNumber = underTest.assignVaultFileNumber("doc-exists");
        underTest.insertDocument(fileNumber, "doc-exists", new ByteArrayInputStream(content));

        assertThat(underTest.documentExists(fileNumber, "doc-exists")).isTrue();
    }

    @Test
    void testDocumentExistsReturnsFalseWhenAbsent() {
        assertThat(underTest.documentExists(1, "nonexistent")).isFalse();
    }

    @Test
    void testDeleteDocument() {
        final byte[] content = {1, 2, 3};
        final int fileNumber = underTest.assignVaultFileNumber("doc-delete");
        underTest.insertDocument(fileNumber, "doc-delete", new ByteArrayInputStream(content));

        underTest.deleteDocument(fileNumber, "doc-delete");

        assertThat(underTest.documentExists(fileNumber, "doc-delete")).isFalse();
    }

    @Test
    void testRotationOnAssign() {
        underTest.close();
        underTest = createManager(3);
        underTest.initialize();

        final int file1 = underTest.assignVaultFileNumber("doc-1");
        final int file2 = underTest.assignVaultFileNumber("doc-2");
        final int file3 = underTest.assignVaultFileNumber("doc-3");

        // All three should be assigned to file 1
        assertThat(file1).isEqualTo(1);
        assertThat(file2).isEqualTo(1);
        assertThat(file3).isEqualTo(1);

        // Fourth assignment triggers rotation
        final int file4 = underTest.assignVaultFileNumber("doc-4");
        assertThat(file4).isEqualTo(2);
        assertThat(tempDir.resolve("vault-000002.db")).exists();
    }

    @Test
    void testInsertToFileAfterRotation() {
        underTest.close();
        underTest = createManager(2);
        underTest.initialize();

        final int fileA = underTest.assignVaultFileNumber("doc-a");
        final int fileB = underTest.assignVaultFileNumber("doc-b");

        // These are in file 1, next assign will rotate
        assertThat(fileA).isEqualTo(1);
        assertThat(fileB).isEqualTo(1);

        final int fileC = underTest.assignVaultFileNumber("doc-c");
        assertThat(fileC).isEqualTo(2);

        // Insert to file 1 after rotation still works
        underTest.insertDocument(fileA, "doc-a", new ByteArrayInputStream(new byte[]{10, 20}));
        underTest.insertDocument(fileB, "doc-b", new ByteArrayInputStream(new byte[]{30, 40}));
        underTest.insertDocument(fileC, "doc-c", new ByteArrayInputStream(new byte[]{50, 60}));

        assertThat(underTest.documentExists(1, "doc-a")).isTrue();
        assertThat(underTest.documentExists(1, "doc-b")).isTrue();
        assertThat(underTest.documentExists(2, "doc-c")).isTrue();
    }

    @Test
    void testReadFromPreviousFileAfterRotation() throws Exception {
        underTest.close();
        underTest = createManager(2);
        underTest.initialize();

        final int fileA = underTest.assignVaultFileNumber("doc-a");
        final int fileB = underTest.assignVaultFileNumber("doc-b");
        underTest.insertDocument(fileA, "doc-a", new ByteArrayInputStream(new byte[]{10, 20}));
        underTest.insertDocument(fileB, "doc-b", new ByteArrayInputStream(new byte[]{30, 40}));

        // Rotation happened, now read from old file
        underTest.assignVaultFileNumber("doc-c");

        try (InputStream result = underTest.readDocument(1, "doc-a")) {
            assertThat(result.readAllBytes()).isEqualTo(new byte[]{10, 20});
        }
    }

    @Test
    void testNewInstancePicksUpExistingState() {
        final byte[] content = {99, 88, 77};
        final int fileNumber = underTest.assignVaultFileNumber("doc-persist");
        underTest.insertDocument(fileNumber, "doc-persist", new ByteArrayInputStream(content));
        underTest.close();

        final SqliteConnectionManager newManager = createManager(100000);
        newManager.initialize();

        try {
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
