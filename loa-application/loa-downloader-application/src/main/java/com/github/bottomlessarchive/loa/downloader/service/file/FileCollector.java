package com.github.bottomlessarchive.loa.downloader.service.file;

import com.github.bottomlessarchive.loa.document.service.domain.DocumentType;
import com.github.bottomlessarchive.loa.downloader.service.file.domain.FileCollectionException;
import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This service is responsible to acquire a file from an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class FileCollector {

    private final FileDownloadManager fileDownloadManager;
    private final FileManipulatorService fileManipulatorService;

    public void acquireFile(final URL fileLocation, final Path resultLocation, final DocumentType documentType) {
        try {
            final String protocol = fileLocation.getProtocol();

            if ("http".equals(protocol) || "https".equals(protocol)) {
                fileDownloadManager.downloadFile(fileLocation, resultLocation);
            } else if ("file".equals(protocol)) {
                fileManipulatorService.copy(Path.of(fileLocation.toURI()), resultLocation);
            }

            if (DocumentType.FB2.equals(documentType) && isZipArchive(resultLocation)) {
                postProcessFB2File(resultLocation);
            }
        } catch (Exception e) {
            throw new FileCollectionException("Failed to collect file at location: " + fileLocation + "!", e);
        }
    }

    private void postProcessFB2File(final Path resultLocation) throws IOException {
        final Path unzipLocation = resultLocation.getParent().resolve(resultLocation.getFileName() + ".tmp");

        try (ZipFile zipFile = new ZipFile(resultLocation.toFile())) {
            // FB2 should have only one file in the archive
            if (zipFile.size() > 1) {
                final ZipEntry fb2FileInArchive = zipFile.entries().nextElement();

                extractFile(zipFile.getInputStream(fb2FileInArchive), unzipLocation);
            }
        }

        Files.move(unzipLocation, resultLocation, StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isZipArchive(final Path filePath) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            final int fileSignature = raf.readInt();

            return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
        } catch (IOException e) {
            // The path is certainly not a zip file if we can't read it
            return false;
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
