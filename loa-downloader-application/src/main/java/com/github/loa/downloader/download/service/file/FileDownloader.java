package com.github.loa.downloader.download.service.file;

import com.github.loa.downloader.download.service.file.domain.FileDownloaderException;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * This service is responsible for downloading files from the internet.
 */
@Service
public class FileDownloader {

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     * @param timeout        the timeout in case if the server did not respond
     */
    public void downloadFile(final URL downloadTarget, final File resultLocation, final int timeout)
            throws FileDownloaderException {
        try {
            final URLConnection urlConnection = downloadTarget.openConnection();
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);

            try (final InputStream inputStream = urlConnection.getInputStream()) {
                try (final FileOutputStream outputStream = new FileOutputStream(resultLocation)) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            }
        } catch (Exception e) {
            throw new FileDownloaderException("Error occurred while downloading file!", e);
        }
    }
}
