package com.github.bottomlessarchive.loa.vault.service.location.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "file", matchIfMissing = true)
public class FileFactory {

    public Path newFile(final String filePath, final String fileName) {
        return Path.of(filePath, fileName);
    }
}
