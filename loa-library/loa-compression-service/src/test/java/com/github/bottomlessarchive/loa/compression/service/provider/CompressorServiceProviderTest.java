package com.github.bottomlessarchive.loa.compression.service.provider;

import com.github.bottomlessarchive.loa.compression.domain.DocumentCompression;
import com.github.bottomlessarchive.loa.compression.service.compressor.BrotliCompressorService;
import com.github.bottomlessarchive.loa.compression.service.compressor.CompressorService;
import com.github.bottomlessarchive.loa.compression.service.compressor.GZIPCompressorService;
import com.github.bottomlessarchive.loa.compression.service.compressor.LZMACompressorService;
import com.github.bottomlessarchive.loa.compression.service.compressor.provider.CompressorServiceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class CompressorServiceProviderTest {

    @Mock
    private GZIPCompressorService gzipCompressionService;

    @Mock
    private LZMACompressorService lzmaCompressionService;

    @Mock
    private BrotliCompressorService brotliCompressorService;

    @InjectMocks
    private CompressorServiceProvider underTest;

    @Test
    void testWhenProviderShouldProvideGzipCompressionService() {
        final CompressorService result = underTest.getCompressionService(DocumentCompression.GZIP);

        assertThat(result, is(gzipCompressionService));
    }

    @Test
    void testWhenProviderShouldProvideLzmaCompressionService() {
        final CompressorService result = underTest.getCompressionService(DocumentCompression.LZMA);

        assertThat(result, is(lzmaCompressionService));
    }

    @Test
    void testWhenProviderShouldProvideBrotliCompressionService() {
        final CompressorService result = underTest.getCompressionService(DocumentCompression.BROTLI);

        assertThat(result, is(brotliCompressorService));
    }
}
