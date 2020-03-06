package com.github.loa.vault.service.location.s3.domain;

import com.github.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

@RequiredArgsConstructor
public class S3VaultLocation implements VaultLocation {

    private final String bucketName;
    private final String fileName;
    private final String contentType;

    @Override
    public OutputStream destination() {
        //TODO: The S3Presigner should be injected!
        try (S3Presigner presigner = S3Presigner.create()) {
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            final PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(putObjectRequest)
                    .build();

            final URL url = presigner.presignPutObject(putObjectPresignRequest).url();

            try {
                final HttpURLConnection connection;
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", contentType);
                connection.setRequestMethod("PUT");

                return connection.getOutputStream();
            } catch (IOException e) {
                //TODO!
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public InputStream content() {
        //TODO: The S3Presigner should be injected!
        try (S3Presigner presigner = S3Presigner.create()) {
            final GetObjectRequest getObjectRequest =
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build();

            final GetObjectPresignRequest getObjectPresignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getObjectRequest)
                            .build();

            final PresignedGetObjectRequest presignedGetObjectRequest =
                    presigner.presignGetObject(getObjectPresignRequest);

            try {
                HttpURLConnection connection = (HttpURLConnection) presignedGetObjectRequest.url().openConnection();

                // Specify any headers that the service needs (not needed when isBrowserExecutable is true)
                presignedGetObjectRequest.httpRequest().headers().forEach((header, values) ->
                        values.forEach(value -> connection.addRequestProperty(header, value)));

                // Send any request payload that the service needs (not needed when isBrowserExecutable is true)
                if (presignedGetObjectRequest.signedPayload().isPresent()) {
                    connection.setDoOutput(true);
                    try (InputStream signedPayload = presignedGetObjectRequest.signedPayload().get().asInputStream();
                         OutputStream httpOutputStream = connection.getOutputStream()) {
                        IoUtils.copy(signedPayload, httpOutputStream);
                    }
                }

                return connection.getInputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void close() {

    }
}
