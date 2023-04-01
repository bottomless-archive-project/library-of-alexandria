package com.github.bottomlessarchive.loa.io.service.collector;

import com.github.bottomlessarchive.loa.file.FileManipulatorService;
import com.github.bottomlessarchive.loa.file.zip.ZipFileManipulatorService;
import com.github.bottomlessarchive.loa.io.service.collector.domain.DocumentCollectionException;
import com.github.bottomlessarchive.loa.io.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * This service is responsible to acquire a file from an {@link URL}.
 */
@Service
@RequiredArgsConstructor
public class DocumentCollector {

    private final FileDownloadManager fileDownloadManager;
    private final FileManipulatorService fileManipulatorService;
    private final ZipFileManipulatorService zipFileManipulatorService;

    public DownloadResult acquireDocument(final String fileLocation, final Path resultLocation, final DocumentType documentType) {
        try {
            final URI documentLocationUri = URI.create(fileLocation);
            final String protocol = documentLocationUri.getScheme();

            DownloadResult documentLocationResultType = DownloadResult.UNKNOWN;

            if ("http".equals(protocol) || "https".equals(protocol)) {
                documentLocationResultType = fileDownloadManager.downloadFile(fileLocation, resultLocation);
            } else if ("file".equals(protocol)) {
                fileManipulatorService.copy(documentLocationUri, resultLocation);
            }

            if (DocumentType.FB2.equals(documentType) && zipFileManipulatorService.isZipArchive(resultLocation)) {
                postProcessFB2File(resultLocation);
            }

            return documentLocationResultType;
        } catch (Exception e) {
            throw new DocumentCollectionException("Failed to collect file at location: " + fileLocation + "!", e);
        }
    }

    private void postProcessFB2File(final Path resultLocation) throws IOException {
        final Path unzipLocation = resultLocation.getParent().resolve(resultLocation.getFileName() + ".tmp");

        zipFileManipulatorService.unzipSingleFileArchive(resultLocation, unzipLocation);

        fileManipulatorService.move(unzipLocation, resultLocation, StandardCopyOption.REPLACE_EXISTING);
    }
}
