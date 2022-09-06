package com.github.bottomlessarchive.loa.url.service.downloader;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentLocationResultCalculatorTest {
/*
    private static final String DOCUMENT_LOCATION_ID = "test-location";
    private static final URL TEST_URL;

    static {
        try {
            TEST_URL = new URL("http://test-url.com/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Mock
    private DocumentLocationManipulator documentLocationManipulator;

    @InjectMocks
    private DocumentLocationResultCalculator underTest;

    @Test
    void testUpdateResultToEmptyBody() {
        underTest.updateResultToEmptyBody(DOCUMENT_LOCATION_ID);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.EMPTY_BODY);
    }

    @Test
    void testUpdateResultToOriginNotFound() {
        underTest.updateResultToOriginNotFound(DOCUMENT_LOCATION_ID);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.ORIGIN_NOT_FOUND);
    }

    @Test
    void testUpdateResultToTimeout() {
        underTest.updateResultToTimeout(DOCUMENT_LOCATION_ID);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.TIMEOUT);
    }

    @Test
    void testUpdateResultToConnectionError() {
        underTest.updateResultToConnectionError(DOCUMENT_LOCATION_ID);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.CONNECTION_ERROR);
    }

    @Test
    void testUpdateResultBasedOnResponseCodeWhenResponseCode200() {
        underTest.calculateResultBasedOnResponseCode(DOCUMENT_LOCATION_ID, TEST_URL, 200);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.OK);
    }

    @Test
    void testUpdateResultBasedOnResponseCodeWhenResponseCode404() {
        underTest.calculateResultBasedOnResponseCode(DOCUMENT_LOCATION_ID, TEST_URL, 404);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.NOT_FOUND);
    }

    @Test
    void testUpdateResultBasedOnResponseCodeWhenResponseCode403() {
        underTest.calculateResultBasedOnResponseCode(DOCUMENT_LOCATION_ID, TEST_URL, 403);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.FORBIDDEN);
    }

    @Test
    void testUpdateResultBasedOnResponseCodeWhenResponseCode400() {
        underTest.calculateResultBasedOnResponseCode(DOCUMENT_LOCATION_ID, TEST_URL, 400);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.SERVER_ERROR);
    }

    @Test
    void testUpdateResultBasedOnResponseCodeWhenResponseCodeIsUnknown() {
        underTest.calculateResultBasedOnResponseCode(DOCUMENT_LOCATION_ID, TEST_URL, -1);

        verify(documentLocationManipulator)
                .updateDownloadResultCode(DOCUMENT_LOCATION_ID, DocumentLocationResultType.UNKNOWN);
    }*/
}
