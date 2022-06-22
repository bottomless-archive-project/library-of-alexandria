package com.github.bottomlessarchive.loa.stage.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

public final class AutoDeleteFileSystemResource extends FileSystemResource {

    public AutoDeleteFileSystemResource(Path filePath) {
        super(filePath);
    }

    @RequiredArgsConstructor
    private static final class AutoDeleteStream extends InputStream {

        private final File file;
        private final InputStream original;

        @Override
        public int read() throws IOException {
            return original.read();
        }

        @Override
        public void close() throws IOException {
            original.close();

            Files.delete(file.toPath());
        }

        @Override
        public int available() throws IOException {
            return original.available();
        }

        @Override
        public int read(@NonNull byte[] b) throws IOException {
            return original.read(b);
        }

        @Override
        public int read(@NonNull byte[] b, int off, int len) throws IOException {
            return original.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return original.skip(n);
        }

        @Override
        public boolean equals(Object obj) {
            return original.equals(obj);
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }

        @Override
        public synchronized void mark(int readlimit) {
            original.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return original.markSupported();
        }

        @Override
        public synchronized void reset() throws IOException {
            original.reset();
        }

        @Override
        public String toString() {
            return original.toString();
        }
    }

    /**
     * @see org.springframework.core.io.FileSystemResource#getInputStream()
     */
    @NonNull
    @Override
    public InputStream getInputStream() throws IOException {
        return new AutoDeleteStream(getFile(), super.getInputStream());
    }
}