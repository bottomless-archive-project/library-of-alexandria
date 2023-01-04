package com.github.bottomlessarchive.loa.file.zip;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ZipFileManipulatorServiceTest {

    private final ZipFileManipulatorService zipFileManipulatorService = new ZipFileManipulatorService();

    @Test
    void testIsZipArchiveWhenFileCannotBeRead() throws IOException {
        final Path testArchive = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        final boolean result = zipFileManipulatorService.isZipArchive(testArchive);

        assertThat(result)
                .isFalse();
    }

    @Test
    void testIsZipArchiveWhenFileIsArchiveWithSignature1() throws IOException {
        final Path testArchive = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        try {
            try (OutputStream testArchiveContent = Files.newOutputStream(testArchive)) {
                testArchiveContent.write(new byte[]{
                        0x50, 0x4B, 0x03, 0x04, 0x00, 0x12, 0x34, 0x56, 0x78
                });
            }

            final boolean result = zipFileManipulatorService.isZipArchive(testArchive);

            assertThat(result)
                    .isTrue();
        } finally {
            Files.delete(testArchive);
        }
    }

    @Test
    void testIsZipArchiveWhenFileIsArchiveWithSignature2() throws IOException {
        final Path testArchive = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        try {
            try (OutputStream testArchiveContent = Files.newOutputStream(testArchive)) {
                testArchiveContent.write(new byte[]{
                        0x50, 0x4B, 0x05, 0x06, 0x00, 0x12, 0x34, 0x56, 0x78
                });
            }

            final boolean result = zipFileManipulatorService.isZipArchive(testArchive);

            assertThat(result)
                    .isTrue();
        } finally {
            Files.delete(testArchive);
        }
    }

    @Test
    void testIsZipArchiveWhenFileIsArchiveWithSignature3() throws IOException {
        final Path testArchive = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        try {
            try (OutputStream testArchiveContent = Files.newOutputStream(testArchive)) {
                testArchiveContent.write(new byte[]{
                        0x50, 0x4B, 0x07, 0x08, 0x00, 0x12, 0x34, 0x56, 0x78
                });
            }

            final boolean result = zipFileManipulatorService.isZipArchive(testArchive);

            assertThat(result)
                    .isTrue();
        } finally {
            Files.delete(testArchive);
        }
    }

    @Test
    void testIsZipArchiveWhenFileIsNotAnArchive() throws IOException {
        final Path testArchive = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        try {
            try (OutputStream testArchiveContent = Files.newOutputStream(testArchive)) {
                testArchiveContent.write(new byte[]{
                        0x50, 0x4B, 0x03, 0x00, 0x12, 0x34, 0x56, 0x78
                });
            }

            final boolean result = zipFileManipulatorService.isZipArchive(testArchive);

            assertThat(result)
                    .isFalse();
        } finally {
            Files.delete(testArchive);
        }
    }

    @Test
    void testUnzipSingleFileArchive() throws IOException {
        final Path testArchive = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        final Path resultFile = Files.createTempFile(UUID.randomUUID().toString(), ".res");

        try {
            try (OutputStream testArchiveContent = Files.newOutputStream(testArchive)) {
                testArchiveContent.write(new byte[]{
                        80, 75, 3, 4, 20, 0, 2, 0, 8, 0, 18, -89, -17, 84, -22, -25, 30, 13, 14, 0, 0, 0, 14, 0, 0, 0, 8, 0, 0, 0, 116,
                        101, 115, 116, 46, 116, 120, 116, 43, -55, -56, 44, 86, 0, -94, 68, -123, -110, -44, -30, 18, 0, 80, 75, 1, 2, 20,
                        0, 20, 0, 2, 0, 8, 0, 18, -89, -17, 84, -22, -25, 30, 13, 14, 0, 0, 0, 14, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 1, 0,
                        32, 0, 0, 0, 0, 0, 0, 0, 116, 101, 115, 116, 46, 116, 120, 116, 80, 75, 5, 6, 0, 0, 0, 0, 1, 0, 1, 0, 54, 0, 0, 0,
                        52, 0, 0, 0, 0, 0
                });
            }

            zipFileManipulatorService.unzipSingleFileArchive(testArchive, resultFile);

            assertThat(Files.readAllBytes(resultFile))
                    .isEqualTo(new byte[]{116, 104, 105, 115, 32, 105, 115, 32, 97, 32, 116, 101, 115, 116});
        } finally {
            Files.delete(testArchive);
            Files.delete(resultFile);
        }
    }
}
