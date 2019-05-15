package com.github.loa.vault.client.configuration;

import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class HttpClientConfiguration {

    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .build();
    }
}
