package com.github.bottomlessarchive.loa.compression.service.provider;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.compression.service.CompressionService;
import com.github.bottomlessarchive.loa.compression.service.GZIPCompressionService;
import com.github.bottomlessarchive.loa.compression.service.LZMACompressionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class CompressionServiceProviderTest {

    @Mock
    private GZIPCompressionService gzipCompressionService;

    @Mock
    private LZMACompressionService lzmaCompressionService;

    @InjectMocks
    private CompressionServiceProvider underTest;

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
