package com.github.bottomlessarchive.loa.staging.service.client;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StagingClient {

    @Qualifier("stagingWebClient")
    private final OkHttpClient okHttpClient;

    public void sendToStaging(final UUID documentId, final InputStream content) {

    }

    public InputStream requestFromStaging(final UUID documentId) {
        return null;
    }
}
