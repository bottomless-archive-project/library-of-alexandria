package com.github.loa.web.view.document.service;

import com.github.loa.web.view.document.service.domain.ImageRenderingException;
import com.mortennobel.imagescaling.ResampleOp;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class DocumentRenderer {

    private final ResampleOp resampleOp;

    public byte[] renderFirstPage(final InputStream documentContent) {
        try (PDDocument document = PDDocument.load(documentContent)) {
            final PDFRenderer pdfRenderer = new PDFRenderer(document);

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);

            final BufferedImage resultImage = resampleOp.filter(image, null);

            ImageIO.write(resultImage, "png", outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ImageRenderingException("Failed to render image!", e);
        }
    }
}
