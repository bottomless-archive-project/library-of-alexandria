package com.github.bottomlessarchive.loa.beacon.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconClientException;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocationResult;
import com.github.bottomlessarchive.loa.beacon.service.client.request.DocumentLocationPartialRequest;
import com.github.bottomlessarchive.loa.beacon.service.client.request.VisitDocumentLocationsRequest;
import com.github.bottomlessarchive.loa.beacon.service.client.response.VisitDocumentLocationsResponse;
import com.github.bottomlessarchive.loa.url.service.downloader.FileDownloadManager;
import com.github.bottomlessarchive.loa.url.service.downloader.domain.DownloadResult;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeaconClient {

    @Qualifier("beaconWebClient")
    private final OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper;
    private final FileDownloadManager fileDownloadManager;

    //TODO: This is temporarily
    private final Map<String, String> beaconApplicationMap = Map.of(
            "beacon-1", "localhost:9996"
    );

    public List<BeaconDocumentLocationResult> visitDocumentLocations(final String beaconId,
            final List<BeaconDocumentLocation> documentLocations) {

        try {
            //TODO: Authentication!
            final Request request = new Request.Builder()
                    .url("http://" + beaconApplicationMap.get(beaconId) + "/beacon/visit-document-locations")
                    .post(
                            createJsonBody(
                                    VisitDocumentLocationsRequest.builder()
                                            .locations(
                                                    documentLocations.stream()
                                                            .map(location ->
                                                                    DocumentLocationPartialRequest.builder()
                                                                            .id(location.getId())
                                                                            .type(location.getType())
                                                                            .location(location.getLocation().toString())
                                                                            .sourceName(location.getSourceName())
                                                                            .build()
                                                            )
                                                            .toList()
                                            )
                                            .build()
                            )
                    )
                    .build();

            final String response = okHttpClient.newCall(request)
                    .execute()
                    .body()
                    .string();

            final VisitDocumentLocationsResponse visitDocumentLocationsResponse = objectMapper.readValue(
                    response, VisitDocumentLocationsResponse.class);

            return visitDocumentLocationsResponse.getResults().stream()
                    .map(location ->
                            BeaconDocumentLocationResult.builder()
                                    .id(location.getId())
                                    .documentId(location.getDocumentId() == null
                                            ? null : UUID.fromString(location.getDocumentId()))
                                    .size(location.getSize())
                                    .checksum(location.getChecksum())
                                    .resultType(location.getResultType())
                                    .sourceName(location.getSourceName())
                                    .type(location.getType())
                                    .build()
                    )
                    .toList();
        } catch (IOException e) {
            throw new BeaconClientException("Failed to send document locations for downloading!", e);
        }
    }

    //TODO: Retry and such
    public DownloadResult downloadDocumentFromBeacon(final String beaconId, final UUID documentId, final Path resultPath) {
        try {
            return fileDownloadManager.downloadFile(new URL("http://" + beaconApplicationMap.get(beaconId) + "/document/" + documentId),
                    resultPath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private RequestBody createJsonBody(final Object requestBody) throws JsonProcessingException {
        return RequestBody.create(objectMapper.writeValueAsBytes(requestBody), MediaType.get("application/json"));
    }
}
