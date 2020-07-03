package com.github.loa.vault.service.location.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@ConditionalOnProperty(value = "loa.vault.location.type", havingValue = "file", matchIfMissing = true)
public class FileFactory {

    public File newFile(final String filePath, final String fileName) {
        return new File(filePath, fileName);
    }
}
