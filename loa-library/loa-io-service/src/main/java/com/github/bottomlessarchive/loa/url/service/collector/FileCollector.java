package com.github.bottomlessarchive.loa.url.service.collector;

import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.file.zip.ZipFileManipulatorService;
import com.github.bottomlessarchive.loa.location.domain.DocumentLocationResultType;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.collector.domain.FileCollectionException;
import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * This service is responsible to acquire a file from an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class FileCollector {

    private final FileDownloadManager fileDownloadManager;
    private final FileManipulatorService fileManipulatorService;
    private final ZipFileManipulatorService zipFileManipulatorService;

    public DocumentLocationResultType acquireFile(final URL fileLocation, final Path resultLocation, final DocumentType documentType) {
        try {
            final String protocol = fileLocation.getProtocol();

            DocumentLocationResultType documentLocationResultType = DocumentLocationResultType.UNKNOWN;

            if ("http".equals(protocol) || "https".equals(protocol)) {
                documentLocationResultType = fileDownloadManager.downloadFile(fileLocation, resultLocation);
            } else if ("file".equals(protocol)) {
                fileManipulatorService.copy(fileLocation.toURI(), resultLocation);
            }

            if (DocumentType.FB2.equals(documentType) && zipFileManipulatorService.isZipArchive(resultLocation)) {
                postProcessFB2File(resultLocation);
            }

            return documentLocationResultType;
        } catch (Exception e) {
            throw new FileCollectionException("Failed to collect file at location: " + fileLocation + "!", e);
        }
    }

    private void postProcessFB2File(final Path resultLocation) throws IOException {
        final Path unzipLocation = resultLocation.getParent().resolve(resultLocation.getFileName() + ".tmp");

        zipFileManipulatorService.unzipSingleFileArchive(resultLocation, unzipLocation);

        fileManipulatorService.move(unzipLocation, resultLocation, StandardCopyOption.REPLACE_EXISTING);
    }
}
