package com.github.loa.downloader.command.batch.filter;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.command.batch.domain.DocumentLocationRecord;
import org.easybatch.core.filter.RecordFilter;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class DocumentLocationRecordFilter implements RecordFilter<DocumentLocationRecord> {

    @Override
    public DocumentLocationRecord processRecord(final DocumentLocationRecord documentLocationRecord) {
        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        final String path = documentLocationRecord.getPayload().getPath();

        return Arrays.stream(DocumentType.values()).anyMatch(documentType ->
                path.endsWith("." + documentType.getFileExtension())) ? documentLocationRecord : null;
    }
}
