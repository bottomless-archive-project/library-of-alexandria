package com.github.bottomlessarchive.loa.conductor.service.client.extension.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InstanceRegistrationContext {

    private final Map<String, String> properties = new HashMap<>();

    public void setProperty(final String propertyName, final String propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
