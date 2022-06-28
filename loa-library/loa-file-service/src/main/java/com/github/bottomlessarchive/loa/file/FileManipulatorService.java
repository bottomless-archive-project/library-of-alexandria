package com.github.bottomlessarchive.loa.file;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileManipulatorService {

    public InputStream getInputStream(final String path) throws IOException {
        return Files.newInputStream(Path.of(path));
    }

    public void delete(final Path path) throws IOException {
        Files.delete(path);
    }
}
