package com.github.bottomlessarchive.loa.url.service.downloader;

import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DocumentLocationResultCalculatorTest {

    @InjectMocks
    private DocumentLocationResultCalculator underTest;

    @Test
    void testTransformExceptionWhenExceptionIsNoRouteToHostException() {
        final DownloadResult result = underTest.transformExceptionToDownloadResult(new NoRouteToHostException());

        assertThat(result)
                .isEqualTo(DownloadResult.ORIGIN_NOT_FOUND);
    }

    @Test
    void testTransformExceptionWhenExceptionIsUnknownHostException() {
        final DownloadResult result = underTest.transformExceptionToDownloadResult(new UnknownHostException());

        assertThat(result)
                .isEqualTo(DownloadResult.ORIGIN_NOT_FOUND);
    }

    @Test
    void testTransformExceptionWhenExceptionIsSocketTimeoutException() {
        final DownloadResult result = underTest.transformExceptionToDownloadResult(new SocketTimeoutException());

        assertThat(result)
                .isEqualTo(DownloadResult.TIMEOUT);
    }

    @Test
    void testTransformExceptionWhenExceptionIsProtocolException() {
        final DownloadResult result = underTest.transformExceptionToDownloadResult(new ProtocolException("unexpected end of stream"));

        assertThat(result)
                .isEqualTo(DownloadResult.CONNECTION_ERROR);
    }

    @Test
    void testCalculateResultBasedOnResponseCodeWhenResponseCode200() {
        final DownloadResult result = underTest.calculateResultBasedOnResponseCode(200);

        assertThat(result)
                .isEqualTo(DownloadResult.OK);
    }

    @Test
    void testCalculateResultBasedOnResponseCodeWhenResponseCode404() {
        final DownloadResult result = underTest.calculateResultBasedOnResponseCode(404);

        assertThat(result)
                .isEqualTo(DownloadResult.NOT_FOUND);
    }

    @Test
    void testCalculateResultBasedOnResponseCodeWhenResponseCode403() {
        final DownloadResult result = underTest.calculateResultBasedOnResponseCode(403);

        assertThat(result)
                .isEqualTo(DownloadResult.FORBIDDEN);
    }

    @Test
    void testCalculateResultBasedOnResponseCodeWhenResponseCode400() {
        final DownloadResult result = underTest.calculateResultBasedOnResponseCode(400);

        assertThat(result)
                .isEqualTo(DownloadResult.SERVER_ERROR);
    }

    @Test
    void testCalculateResultBasedOnResponseCodeWhenResponseCodeIsUnknown() {
        final DownloadResult result = underTest.calculateResultBasedOnResponseCode(-1);

        assertThat(result)
                .isEqualTo(DownloadResult.UNKNOWN);
    }
}
