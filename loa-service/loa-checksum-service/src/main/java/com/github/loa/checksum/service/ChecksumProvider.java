package com.github.loa.checksum.service;

import com.github.loa.document.service.domain.DocumentType;

/**
 * This interface is responsible for providing checksum values for files in the stage location.
 */
public interface ChecksumProvider {

    String checksum(String documentId, DocumentType documentType);
}
