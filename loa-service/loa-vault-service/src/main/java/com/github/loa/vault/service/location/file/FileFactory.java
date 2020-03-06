package com.github.loa.vault.service.location.file;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileFactory {

    public File newFile(final String filePath, final String fileName) {
        return new File(filePath, fileName);
    }
}
