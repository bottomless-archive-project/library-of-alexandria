package com.github.bottomlessarchive.loa.renderer.service;

import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class PageRendererService {

    @SneakyThrows
    public BufferedImage renderPage(final PDDocument document, final int pageIndex) {
        final PDFRenderer pdfRenderer = new PDFRenderer(document);

        return pdfRenderer.renderImageWithDPI(pageIndex, 300);
    }
}
