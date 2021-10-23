package com.github.bottomlessarchive.loa.web.view.document.service;

import com.github.bottomlessarchive.loa.web.view.document.service.domain.ImageRenderingException;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocumentRenderer {

    public byte[] renderFirstPage(final InputStream documentContent) {
        try (PDDocument document = PDDocument.load(documentContent); documentContent) {
            final PDFRenderer pdfRenderer = new PDFRenderer(document);

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);

            Thumbnails.of(image)
                    .size(248, 350)
                    .outputFormat("png")
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageRenderingException("Failed to render image!", e);
        }
    }
}
