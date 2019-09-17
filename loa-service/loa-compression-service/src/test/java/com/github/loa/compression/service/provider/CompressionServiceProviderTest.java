package com.github.loa.compression.service.provider;

import com.github.loa.compression.domain.DocumentCompression;
import com.github.loa.compression.service.CompressionService;
import com.github.loa.compression.service.GZIPCompressionService;
import com.github.loa.compression.service.LZMACompressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class CompressionServiceProviderTest {

    private GZIPCompressionService gzipCompressionService;
    private LZMACompressionService lzmaCompressionService;
    private CompressionServiceProvider underTest;

    @BeforeEach
    private void setup() {
        gzipCompressionService = mock(GZIPCompressionService.class);
        lzmaCompressionService = mock(LZMACompressionService.class);

        underTest = new CompressionServiceProvider(gzipCompressionService, lzmaCompressionService);
    }

    @Test
    void testWhenProviderShouldProvideGzipCompressionService() {
        final CompressionService result = underTest.getCompressionService(DocumentCompression.GZIP);

        assertThat(result, is(gzipCompressionService));
    }

    @Test
    void testWhenProviderShouldProvideLzmaCompressionService() {
        final CompressionService result = underTest.getCompressionService(DocumentCompression.LZMA);

        assertThat(result, is(lzmaCompressionService));
    }
}
