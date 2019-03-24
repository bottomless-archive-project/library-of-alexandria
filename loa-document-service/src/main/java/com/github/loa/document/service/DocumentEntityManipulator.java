package com.github.loa.document.service;

import com.github.loa.document.repository.DocumentRepository;
import com.github.loa.document.service.domain.DocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentEntityManipulator {

    private final DocumentRepository documentRepository;

    public void updateStatus(final String documentId, final DocumentStatus documentStatus) {
        documentRepository.updateStatus(documentId, documentStatus.toString());
    }

    public void updateFileSizeAndCrc(final String documentId, final long fileSize, final String crc) {
        documentRepository.updateFileSizeAndCrc(documentId, fileSize, crc);
    }
}
