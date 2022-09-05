package com.github.bottomlessarchive.loa.location.service.validation.extension;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultFileExtensionValidator implements FileExtensionValidator {

    private final DocumentType documentType;

    @Override
    public boolean isValidPathWithExtension(final String path) {
        return path.endsWith("." + documentType.getFileExtension());
    }
}
