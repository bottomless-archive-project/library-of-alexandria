package com.github.bottomlessarchive.loa.renderer.service;

import com.github.bottomlessarchive.loa.renderer.service.domain.ImageRenderingException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocumentRendererService {

    private final ThumbnailService thumbnailService;
    private final PageRendererService pageRendererService;

    public byte[] renderFirstPage(final InputStream documentContent) {
        try (PDDocument document = PDDocument.load(documentContent); documentContent) {
            return thumbnailService.createThumbnail(pageRendererService.renderPage(document, 0));
        } catch (IOException e) {
            throw new ImageRenderingException("Failed to render image!", e);
        }
    }
}
