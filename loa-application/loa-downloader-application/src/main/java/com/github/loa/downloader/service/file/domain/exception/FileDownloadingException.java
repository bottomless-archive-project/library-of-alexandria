package com.github.loa.downloader.service.file.domain.exception;

public class FileDownloadingException extends RuntimeException {

    public FileDownloadingException(final Throwable throwable) {
        super(throwable);
    }
}
