package com.github.bottomlessarchive.loa.conductor.service.client.extension;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceRegistrationContext;

public interface InstanceRegistrationExtensionProvider {

    void extendRegistration(InstanceRegistrationContext instanceRegistrationContext);
}
