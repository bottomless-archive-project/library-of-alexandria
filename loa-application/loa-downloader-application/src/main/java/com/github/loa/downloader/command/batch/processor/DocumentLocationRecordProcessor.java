package com.github.loa.downloader.command.batch.processor;

import com.github.loa.downloader.command.batch.domain.DocumentLocationRecord;
import com.github.loa.downloader.download.service.document.DocumentDownloader;
import lombok.RequiredArgsConstructor;
import org.easybatch.core.processor.RecordProcessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentLocationRecordProcessor implements RecordProcessor<DocumentLocationRecord, DocumentLocationRecord> {

    private final DocumentDownloader documentDownloader;

    @Override
    public DocumentLocationRecord processRecord(DocumentLocationRecord documentLocationRecord) {
        documentDownloader.downloadDocument(documentLocationRecord.getPayload());

        return documentLocationRecord;
    }
}
