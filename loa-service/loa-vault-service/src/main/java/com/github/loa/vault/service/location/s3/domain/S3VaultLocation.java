package com.github.loa.vault.service.location.s3.domain;

import com.github.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.StorageClass;

import java.io.InputStream;

@RequiredArgsConstructor
public class S3VaultLocation implements VaultLocation {

    private final String bucketName;
    private final String fileName;
    private final String contentType;
    private final S3Client s3Client;

    @Override
    public void upload(final byte[] documentContents) {
        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .storageClass(StorageClass.REDUCED_REDUNDANCY)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(documentContents));
    }

    @Override
    public InputStream download() {
        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        return s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());
    }

    @Override
    public void clear() {
        final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
