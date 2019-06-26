package com.github.loa.downloader.command.batch.domain;

import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Header;

import java.net.URL;

public class DocumentLocationRecord extends GenericRecord<URL> {

    public DocumentLocationRecord(final Header header, final URL payload) {
        super(header, payload);
    }
}
