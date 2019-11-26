package com.github.loa.queue.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DownloaderQueueConfiguration {

    private final DownloaderQueueConfigurationProperties downloaderQueueConfigurationProperties;

    @Bean
    public ClientSessionFactory clientSession(final ServerLocator serverLocator) throws Exception {
        return serverLocator.createSessionFactory();
    }

    @Bean
    protected ServerLocator serverLocator(final TransportConfiguration transportConfiguration) {
        return ActiveMQClient.createServerLocatorWithoutHA(transportConfiguration);
    }

    @Bean
    protected TransportConfiguration serverTransportConfiguration() {
        return new TransportConfiguration(NettyConnectorFactory.class.getName(),
                Map.of(
                        TransportConstants.HOST_PROP_NAME, downloaderQueueConfigurationProperties.getHost(),
                        TransportConstants.PORT_PROP_NAME, downloaderQueueConfigurationProperties.getPort()
                )
        );
    }
}
