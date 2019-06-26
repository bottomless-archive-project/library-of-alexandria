package com.github.loa.downloader.command.batch.mapper;

import com.github.loa.downloader.command.batch.domain.DocumentLocationRecord;
import lombok.extern.slf4j.Slf4j;
import org.easybatch.core.mapper.RecordMapper;
import org.easybatch.core.record.StringRecord;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
public class DocumentLocationRecordMapper implements RecordMapper<StringRecord, DocumentLocationRecord> {

    @Override
    public DocumentLocationRecord processRecord(final StringRecord record) throws MalformedURLException {
        try {
            final URL location = new URL(record.getPayload());

            return new DocumentLocationRecord(record.getHeader(), location);
        } catch (MalformedURLException e) {
            log.warn("Unable to parse url with location: " + record.getPayload(), e);

            throw e;
        }
    }
}
