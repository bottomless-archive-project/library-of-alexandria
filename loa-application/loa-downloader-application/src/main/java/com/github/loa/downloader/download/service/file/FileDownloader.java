package com.github.loa.downloader.download.service.file;

import com.github.loa.downloader.download.service.file.domain.FileDownloaderException;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * This service is responsible for downloading files from the internet.
 */
@Service
public class FileDownloader {

    private static final String GZIP_ENCODING = "gzip";

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
            urlConnection.setRequestProperty("Accept-Encoding", GZIP_ENCODING);

            try (final InputStream inputStream = buildInputStream(urlConnection)) {
                try (final FileOutputStream outputStream = new FileOutputStream(resultLocation)) {
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            }
        } catch (Exception e) {
            throw new FileDownloaderException("Error occurred while downloading file!", e);
        }
    }

    private InputStream buildInputStream(final URLConnection urlConnection) throws IOException {
        return GZIP_ENCODING.equals(urlConnection.getContentEncoding()) ?
                new GZIPInputStream(urlConnection.getInputStream()) : urlConnection.getInputStream();
    }
}
