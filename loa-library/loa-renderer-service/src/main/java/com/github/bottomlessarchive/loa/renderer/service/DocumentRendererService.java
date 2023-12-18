package com.github.bottomlessarchive.loa.renderer.service;

import com.github.bottomlessarchive.loa.renderer.service.domain.ImageRenderingException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
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
        try (RandomAccessReadBuffer randomAccessReadBuffer = new RandomAccessReadBuffer(documentContent);
             PDDocument document = Loader.loadPDF(randomAccessReadBuffer); documentContent) {
            return thumbnailService.createThumbnail(pageRendererService.renderPage(document, 0));
        } catch (IOException e) {
            throw new ImageRenderingException("Failed to render image!", e);
        }
    }
}
