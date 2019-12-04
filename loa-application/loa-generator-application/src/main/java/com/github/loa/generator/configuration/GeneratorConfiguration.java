package com.github.loa.generator.configuration;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneratorConfiguration {

    public static final String QUEUE_ADDRESS = "loa-document-location";
    public static final String QUEUE_NAME = "loa-document-location";

    @Bean
    public ClientSession clientSession(final ClientSessionFactory clientSessionFactory) throws ActiveMQException {
        return clientSessionFactory.createSession();
    }

    @Bean
    public ClientProducer clientProducer(final ClientSession clientSession) throws ActiveMQException {
        return clientSession.createProducer(QUEUE_ADDRESS);
    }
}
