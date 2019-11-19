package com.github.loa.queue.configuration;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArtemisQueueConfiguration implements ArtemisConfigurationCustomizer {

    @Override
    public void customize(final org.apache.activemq.artemis.core.config.Configuration configuration) {
        // Making the configuration non-inVM configuration so the server could be connected by outside applications
        configuration.addConnectorConfiguration("netty-connector", new TransportConfiguration(
                NettyConnectorFactory.class.getName()));
        configuration.addAcceptorConfiguration(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
    }
}
