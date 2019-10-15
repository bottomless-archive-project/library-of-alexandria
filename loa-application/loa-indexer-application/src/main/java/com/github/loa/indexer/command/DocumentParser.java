package com.github.loa.indexer.command;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DocumentParser {

    public PDDocument parseDocument(final byte[] documentContents) {
        try {
            return PDDocument.load(documentContents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
