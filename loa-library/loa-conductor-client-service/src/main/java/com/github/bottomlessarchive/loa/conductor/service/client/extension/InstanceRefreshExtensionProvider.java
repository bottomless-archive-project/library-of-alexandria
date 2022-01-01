package com.github.bottomlessarchive.loa.conductor.service.client.extension;

import com.github.bottomlessarchive.loa.conductor.service.client.extension.domain.InstanceRefreshContext;

public interface InstanceRefreshExtensionProvider {

    void extendRegistration(InstanceRefreshContext instanceRefreshContext);
}
