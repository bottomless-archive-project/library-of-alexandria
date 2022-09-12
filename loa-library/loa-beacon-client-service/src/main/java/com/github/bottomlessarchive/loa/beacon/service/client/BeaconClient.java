package com.github.bottomlessarchive.loa.beacon.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconClientException;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocation;
import com.github.bottomlessarchive.loa.beacon.service.client.domain.BeaconDocumentLocationResult;
import com.github.bottomlessarchive.loa.beacon.service.client.request.DocumentLocationPartialRequest;
import com.github.bottomlessarchive.loa.beacon.service.client.request.VisitDocumentLocationsRequest;
import com.github.bottomlessarchive.loa.beacon.service.client.response.VisitDocumentLocationsResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BeaconClient {

    @Qualifier("beaconWebClient")
    private final OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper;

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
                                                                            .location(location.getLocation())
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

            //TODO: Map the response to a domain class
        } catch (IOException e) {
            throw new BeaconClientException("Failed to send document locations for downloading!", e);
        }
    }

    private RequestBody createJsonBody(final Object requestBody) throws JsonProcessingException {
        return RequestBody.create(objectMapper.writeValueAsBytes(requestBody), MediaType.get("application/json"));
    }
}
