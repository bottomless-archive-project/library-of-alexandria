package com.github.bottomlessarchive.loa.conductor.service.client.extension;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceExtensionContext;

public interface InstancePropertyExtensionProvider {

    void extendInstanceWithProperty(InstanceExtensionContext instanceExtensionContext);
}
