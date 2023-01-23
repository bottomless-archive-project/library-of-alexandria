package com.github.bottomlessarchive.loa.conductor.service;

import com.github.bottomlessarchive.loa.application.domain.ApplicationType;

import java.util.Locale;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

public class ConductorClientTestUtility {

    public static void expectRegisterServiceCall(final ApplicationType applicationType) {
        stubFor(
                post("/service/" + applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH))
                        .willReturn(
                                ok()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"id\":\"" + UUID.randomUUID() + "\"}")
                        )
        );
    }

    public static void expectQueryServiceCall(final ApplicationType applicationType, final String host, final int port) {
        stubFor(
                get("/service/" + applicationType.name().replaceAll("_", "-").toLowerCase(Locale.ENGLISH))
                        .willReturn(
                                ok()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"applicationType\":\"" + applicationType + "\",\"instances\":[{\"id\":"
                                                + "\"1e0d6192-5a47-4dfa-b8d3-ba64f01de11a\",\"location\":\"" + host + "\",\"port\":"
                                                + port + ",\"lastHeartbeat\":\"2022-12-03T18:23:40.319067100Z\",\"properties\":[{"
                                                + "\"name\":\"archivingQueueCount\",\"value\":\"-1\"},{\"name\":\"locationQueueCount\","
                                                + "\"value\":\"-1\"}]}]}")
                        )
        );
    }

}
