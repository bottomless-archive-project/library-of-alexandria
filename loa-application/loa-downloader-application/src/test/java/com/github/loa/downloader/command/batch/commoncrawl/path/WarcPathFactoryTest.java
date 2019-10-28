package com.github.loa.downloader.command.batch.commoncrawl.path;

import com.github.loa.url.service.StreamFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

//TODO: More tests needed!
class WarcPathFactoryTest {

    private static final String WARC_BATCH_ID = "test";

    private StreamFactory streamFactoryMock;
    private WarcPathFactory warcPathFactory;

    @BeforeEach
    public void setup() throws IOException {
        streamFactoryMock = mock(StreamFactory.class);
        when(streamFactoryMock.openGZIPLocation("https://commoncrawl.s3.amazonaws.com/crawl-data/" + WARC_BATCH_ID
                + "/warc.paths.gz")).thenReturn(new ByteArrayInputStream(new byte[]{}));

        warcPathFactory = new WarcPathFactory(streamFactoryMock);
    }

    @Test
    public void testNewPaths() throws IOException {
        warcPathFactory.newPaths(WARC_BATCH_ID);

        verify(streamFactoryMock).openGZIPLocation("https://commoncrawl.s3.amazonaws.com/crawl-data/" + WARC_BATCH_ID
                + "/warc.paths.gz");
    }
}