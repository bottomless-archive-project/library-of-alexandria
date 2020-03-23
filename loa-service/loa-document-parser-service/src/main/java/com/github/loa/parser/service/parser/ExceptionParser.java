package com.github.loa.parser.service.parser;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.ContentHandler;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class ExceptionParser implements Parser {

    @Override
    public Set<MediaType> getSupportedTypes(final ParseContext parseContext) {
        return Collections.emptySet();
    }

    @Override
    public void parse(final InputStream inputStream, final ContentHandler contentHandler, final Metadata metadata,
            final ParseContext parseContext) throws TikaException {
        throw new TikaException("Unable to parse content! Unknown document type!");
    }
}
