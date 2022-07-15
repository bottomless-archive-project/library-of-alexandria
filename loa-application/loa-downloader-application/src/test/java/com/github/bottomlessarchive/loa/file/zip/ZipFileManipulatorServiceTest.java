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
}