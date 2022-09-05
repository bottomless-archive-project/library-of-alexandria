package com.github.bottomlessarchive.loa.location.service.validation.extension;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DelegatingFileExtensionValidator implements FileExtensionValidator {

    private final List<FileExtensionValidator> fileExtensionValidators = Stream.concat(
                    Arrays.stream(DocumentType.values())
                            .filter(documentType -> documentType != DocumentType.FB2)
                            .map(DefaultFileExtensionValidator::new),
                    Stream.of(new FictionBook2FileExtensionValidator())
            )
            .toList();

    @Override
    public boolean isValidPathWithExtension(final String path) {
        return fileExtensionValidators.stream()
                .anyMatch(validator -> validator.isValidPathWithExtension(path));
    }
}
