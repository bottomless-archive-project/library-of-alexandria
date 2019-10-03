package com.github.loa.administrator.command.document.pagecount;

import com.github.loa.administrator.command.document.pagecount.domain.PageCountDocument;
import com.github.loa.document.service.DocumentManipulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageCountProcessor {

    private final DocumentManipulator documentManipulator;

    public void processDocument(final PageCountDocument document) {
        try (final PDDocument pdfDocument = PDDocument.load(document.getDocumentContents())) {
            documentManipulator.updatePageCount(document.getDocumentEntity().getId(), pdfDocument.getNumberOfPages());
        } catch (IOException e) {
            log.error("Failed to parse document {}!", document.getDocumentEntity().getId(), e);
        }
    }
}
