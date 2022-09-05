package com.github.bottomlessarchive.loa.location.service.validation.extension;

import com.github.bottomlessarchive.loa.type.domain.DocumentType;

public class FictionBook2FileExtensionValidator implements FileExtensionValidator {

    @Override
    public boolean isValidPathWithExtension(final String path) {
        return path.endsWith("." + DocumentType.FB2.getFileExtension())
                || path.endsWith("." + DocumentType.FB2.getFileExtension() + ".zip");
    }
}
