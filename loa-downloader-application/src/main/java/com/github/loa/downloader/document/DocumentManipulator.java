package com.github.loa.downloader.document;

import com.github.loa.downloader.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentManipulator {

    private final DocumentRepository documentRepositor;

    public void updateStatus() {

    }

    public void updateCrcAndFileSize() {

    }
}
