package com.github.bottomlessarchive.loa.file;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileManipulatorService {

    public InputStream getInputStream(final String path) throws IOException {
        return getInputStream(Path.of(path));
    }

    public InputStream getInputStream(final Path path) throws IOException {
        return Files.newInputStream(path);
    }

    public void delete(final Path path) throws IOException {
        Files.delete(path);
    }

    public long size(final Path path) throws IOException {
        return Files.size(path);
    }

    public void copy(final URI copyFrom, final Path copyTo) throws IOException {
        copy(Path.of(copyFrom), copyTo);
    }

    public void copy(final Path copyFrom, final Path copyTo) throws IOException {
        Files.copy(copyFrom, copyTo);
    }

    public void move(final Path sourcePath, final Path destinationPath, final CopyOption... copyOptions) throws IOException {
        Files.move(sourcePath, destinationPath, copyOptions);
    }

    public Path newFile(final String filePath, final String fileName) {
        return Path.of(filePath, fileName);
    }
}
