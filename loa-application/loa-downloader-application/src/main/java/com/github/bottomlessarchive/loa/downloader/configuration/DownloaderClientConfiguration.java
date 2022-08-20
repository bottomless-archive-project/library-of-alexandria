package com.github.bottomlessarchive.loa.downloader.configuration;

import lombok.RequiredArgsConstructor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import com.github.bottomlessarchive.loa.downloader.configuration.ssl.TrustAllTrustManager;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class is responsible to create the web client that is used to download documents from the internet.
 */
@Configuration
@RequiredArgsConstructor
public class DownloaderClientConfiguration {

    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    @Bean
    public OkHttpClient downloaderClient(
            @Qualifier("dispatcher") final Dispatcher dispatcher,
            @Qualifier("connectionPool") final ConnectionPool connectionPool,
            @Qualifier("trustAllSSLContext") final SSLContext trustAllSSLContext,
            @Qualifier("trustAllTrustManagers") final TrustManager[] trustAllTrustManagers) {
        return new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                // We don't care about hostname verification when download files. See #441 for more info.
                .sslSocketFactory(trustAllSSLContext.getSocketFactory(), (X509TrustManager) trustAllTrustManagers[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    @Bean
    protected Dispatcher dispatcher() {
        final Dispatcher dispatcher = new Dispatcher();

        dispatcher.setMaxRequests(downloaderConfigurationProperties.parallelism());
        dispatcher.setMaxRequestsPerHost(10);

        return dispatcher;
    }

    @Bean
    protected ConnectionPool connectionPool() {
        return new ConnectionPool(downloaderConfigurationProperties.parallelism(), 5, TimeUnit.MINUTES);
    }

    @Bean
    @SneakyThrows
    protected SSLContext trustAllSSLContext(@Qualifier("trustAllTrustManagers") final TrustManager[] trustAllTrustManagers) {
        final SSLContext sslContext = SSLContext.getInstance("SSL");

        // Disabling SSL trust management & checking. See #441 for more info.
        sslContext.init(null, trustAllTrustManagers, new java.security.SecureRandom());

        return sslContext;
    }

    @Bean
    protected TrustManager[] trustAllTrustManagers(final TrustAllTrustManager trustAllTrustManager) {
        return new TrustManager[]{trustAllTrustManager};
    }
}
