package com.github.loa.checksum.service;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.checksum.type", havingValue = "sha256")
public class Sha256ChecksumProvider implements ChecksumProvider {

    private final StageLocationFactory stageLocationFactory;

    @Override
    public Mono<String> checksum(final String documentId, final DocumentType documentType) {
        return stageLocationFactory.getLocation(documentId, documentType)
                .flatMap(documentLocation -> Mono.justOrEmpty(convert(documentLocation)));
    }

    private Optional<String> convert(final File documentLocation) {
        try (final BufferedInputStream documentInputStream = new BufferedInputStream(
                new FileInputStream(documentLocation))) {
            return Optional.of(DigestUtils.sha256Hex(documentInputStream));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
