package com.github.loa.downloader.command.batch.reader;

import com.github.loa.source.configuration.file.FileDocumentSourceConfiguration;
import com.github.loa.source.service.file.FileSourceFactory;
import lombok.RequiredArgsConstructor;
import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.Record;
import org.easybatch.core.record.StringRecord;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.source.type", havingValue = "file")
public class FileDocumentLocationRecordReader implements RecordReader {

    private final FileDocumentSourceConfiguration fileDocumentSourceConfiguration;
    private final FileSourceFactory fileSourceFactory;

    private BufferedReader reader;
    private long line = 1;

    @Override
    public void open() {
        reader = new BufferedReader(new InputStreamReader(fileSourceFactory.newInputStream(
                fileDocumentSourceConfiguration.getLocation())));
    }

    @Override
    public Record readRecord() throws Exception {
        final String recordValue = reader.readLine();

        return recordValue != null ? new StringRecord(new Header(line++, "file", new Date()), recordValue) : null;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
