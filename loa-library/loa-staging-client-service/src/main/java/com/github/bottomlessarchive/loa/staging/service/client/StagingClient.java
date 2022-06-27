package com.github.bottomlessarchive.loa.staging.service.client;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;
import com.github.bottomlessarchive.loa.conductor.service.client.ConductorClient;
import com.github.bottomlessarchive.loa.conductor.service.domain.ServiceInstanceEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StagingClient {

    private static final MediaType OCTET_STREAM_MEDIA_TYPE = MediaType.parse("application/octet-stream");

    @Qualifier("stagingWebClient")
    private final OkHttpClient okHttpClient;
    private final ConductorClient conductorClient;

    @SneakyThrows //TODO: StagingException
    public void moveToStaging(final UUID documentId, final Path content) {
        final ServiceInstanceEntity serviceInstanceEntity = conductorClient.getInstanceOrBlock(ApplicationType.STAGING_APPLICATION);

        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", documentId.toString(),
                        RequestBody.create(content.toFile(), OCTET_STREAM_MEDIA_TYPE))
                .build();

        final Request request = new Request.Builder()
                .url(serviceInstanceEntity.getAsHttpLocation() + "/document/" + documentId)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request)
                .execute()
                .close();
    }

    @SneakyThrows //TODO: StagingException
    public InputStream grabFromStaging(final UUID documentId) {
        final ServiceInstanceEntity serviceInstanceEntity = conductorClient.getInstanceOrBlock(ApplicationType.STAGING_APPLICATION);

        final Request request = new Request.Builder()
                .url(serviceInstanceEntity.getAsHttpLocation() + "/document/" + documentId)
                .get()
                .build();

        return okHttpClient.newCall(request)
                .execute()
                .body()
                .byteStream();
    }
}
