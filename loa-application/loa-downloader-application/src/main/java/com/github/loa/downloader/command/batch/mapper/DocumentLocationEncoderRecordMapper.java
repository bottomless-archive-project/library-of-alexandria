package com.github.loa.downloader.command.batch.mapper;

import com.github.loa.downloader.command.batch.domain.DocumentLocationRecord;
import com.github.loa.url.service.UrlEncoder;
import lombok.RequiredArgsConstructor;
import org.easybatch.core.mapper.RecordMapper;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentLocationEncoderRecordMapper implements RecordMapper<DocumentLocationRecord, DocumentLocationRecord> {

    private final UrlEncoder urlEncoder;

    @Override
    public DocumentLocationRecord processRecord(final DocumentLocationRecord record) {
        final Optional<URL> url = urlEncoder.encode(record.getPayload());

        return url.map(resultUrl -> new DocumentLocationRecord(record.getHeader(), resultUrl))
                .orElseThrow(() -> new RuntimeException("Invalid url!"));
    }
}
