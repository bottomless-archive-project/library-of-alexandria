package com.github.loa.queue.service;

import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.config.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ArtemisQueueInitializer implements CommandLineRunner {

    @Override
    public void run(final String... args) throws Exception {
        final Configuration configuration = new ConfigurationImpl();

        configuration.setSecurityEnabled(false);
        configuration.addConnectorConfiguration("netty-connector", new TransportConfiguration(
                NettyConnectorFactory.class.getName()));
        configuration.addAcceptorConfiguration(new TransportConfiguration(NettyAcceptorFactory.class.getName()));

        final EmbeddedActiveMQ embedded = new EmbeddedActiveMQ();
        embedded.setConfiguration(configuration);

        embedded.start();

        /*embedded.getActiveMQServer().createQueue(new SimpleString("loa.downloader"), RoutingType.ANYCAST,
                new SimpleString("loa.downloader"), null, true, false);*/
    }
}
