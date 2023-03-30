package com.github.bottomlessarchive.loa.renderer.service;

import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
public class ThumbnailService {

    @SneakyThrows
    public byte[] createThumbnail(final BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .size(248, 350)
                    .outputFormat("png")
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }
}
