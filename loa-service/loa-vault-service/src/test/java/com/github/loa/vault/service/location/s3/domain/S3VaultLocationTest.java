package com.github.loa.vault.service.location.s3.domain;

import com.github.loa.compression.domain.DocumentCompression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.StorageClass;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3VaultLocationTest {

    private static final String BUCKET_NAME = "test-bucket";
    private static final String FILE_NAME = "example.pdf";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final byte[] CONTENT = {1, 2, 3, 4, 5};

    @Mock
    private S3Client s3Client;

    @Captor
    private ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<GetObjectRequest> getObjectRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<RequestBody> requestBodyArgumentCaptor;

    private S3VaultLocation underTest;

    @BeforeEach
    void setup() {
        underTest = new S3VaultLocation(BUCKET_NAME, FILE_NAME, CONTENT_TYPE, s3Client, DocumentCompression.NONE);
    }

    @Test
    void testUpload() throws IOException {
        underTest.upload(CONTENT);

        verify(s3Client).putObject(putObjectRequestArgumentCaptor.capture(), requestBodyArgumentCaptor.capture());

        final PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
        assertThat(putObjectRequest.bucket(), is(BUCKET_NAME));
        assertThat(putObjectRequest.key(), is(FILE_NAME));
        assertThat(putObjectRequest.contentType(), is(CONTENT_TYPE));
        assertThat(putObjectRequest.storageClass(), is(StorageClass.REDUCED_REDUNDANCY));

        final RequestBody requestBody = requestBodyArgumentCaptor.getValue();
        assertThat(requestBody.contentStreamProvider().newStream().readAllBytes(), is(CONTENT));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDownload() {
        final InputStream downloadResult = mock(InputStream.class);
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
                .thenReturn(downloadResult);

        final InputStream result = underTest.download();

        verify(s3Client).getObject(getObjectRequestArgumentCaptor.capture(), any(ResponseTransformer.class));

        final GetObjectRequest getObjectRequest = getObjectRequestArgumentCaptor.getValue();
        assertThat(getObjectRequest.bucket(), is(BUCKET_NAME));
        assertThat(getObjectRequest.key(), is(FILE_NAME));

        assertThat(result, is(downloadResult));
    }
}
