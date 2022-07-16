package com.github.bottomlessarchive.loa.file.zip;

import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ZipFileManipulatorService {

    public boolean isZipArchive(final Path filePath) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            final int fileSignature = raf.readInt();

            return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
        } catch (IOException e) {
            // The path is certainly not a zip file if we can't read it
            return false;
        }
    }

    public void unzipSingleFileArchive(final Path sourcePath, final Path destinationPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(sourcePath.toFile())) {
            // FB2 should have only one file in the archive
            if (zipFile.size() == 1) {
                final ZipEntry fb2FileInArchive = zipFile.entries().nextElement();

                extractFile(zipFile.getInputStream(fb2FileInArchive), destinationPath);
            }
        }
    }

    private void extractFile(final InputStream zipIn, final Path filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath))) {
            final byte[] bytesIn = new byte[8192];

            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
