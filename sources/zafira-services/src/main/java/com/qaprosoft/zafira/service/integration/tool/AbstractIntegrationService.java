/*******************************************************************************
 * Copyright 2013-2019 Qaprosoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.zafira.service.integration.tool;

import com.qaprosoft.zafira.models.entity.integration.Integration;
import com.qaprosoft.zafira.service.exception.IntegrationException;
import com.qaprosoft.zafira.service.integration.IntegrationService;
import com.qaprosoft.zafira.service.integration.tool.adapter.IntegrationAdapter;
import com.qaprosoft.zafira.service.integration.tool.adapter.IntegrationGroupAdapter;
import com.qaprosoft.zafira.service.integration.tool.proxy.IntegrationAdapterProxy;

import java.util.Optional;

public abstract class AbstractIntegrationService<T extends IntegrationGroupAdapter> {

    private static final String ERR_MSG_ADAPTER_NOT_FOUND = "Requested adapter with id %s can not be found";
    private static final String ERR_MSG_ADAPTER_NOT_FOUND_BY_TYPE = "Requested adapter of type %s can not be found";

    private final IntegrationService integrationService;
    private final IntegrationAdapterProxy integrationAdapterProxy;
    private final String defaultType;

    public AbstractIntegrationService(IntegrationService integrationService, IntegrationAdapterProxy integrationAdapterProxy, String defaultType) {
        this.integrationService = integrationService;
        this.integrationAdapterProxy = integrationAdapterProxy;
        this.defaultType = defaultType;
    }

    public boolean isEnabledAndConnected(Long integrationId) {
        Optional<IntegrationAdapter> maybeAdapter = getAdapter(integrationId);
        if (maybeAdapter.isEmpty()) {
            return false;
        }

        IntegrationAdapter adapter = maybeAdapter.get();
        // adapter presence is already verified
        Long adapterIntegrationId = adapter.getIntegrationId();
        // we now use proper integration id since it can be null prior to this point, but never for adapter
        Integration integration = integrationService.retrieveById(adapterIntegrationId);

        return integration.isEnabled() && adapter.isConnected();
    }

    @SuppressWarnings("unchecked")
    public T getAdapterByIntegrationId(Long integrationId) {
        Optional<IntegrationAdapter> maybeAdapter = getAdapter(integrationId);
        if (integrationId == null) {
            return (T) maybeAdapter.orElseThrow(() -> new IntegrationException(String.format(ERR_MSG_ADAPTER_NOT_FOUND_BY_TYPE, defaultType)));
        } else {
            return (T) maybeAdapter.orElseThrow(() -> new IntegrationException(String.format(ERR_MSG_ADAPTER_NOT_FOUND, integrationId)));
        }
    }

    private Optional<IntegrationAdapter> getAdapter(Long integrationId) {
        Optional<IntegrationAdapter> maybeAdapter;
        if (integrationId == null) {
            // can be null in case of legacy client call - use default adapter
            maybeAdapter = integrationAdapterProxy.getDefaultAdapter(defaultType);
        } else {
            maybeAdapter = IntegrationAdapterProxy.getAdapter(integrationId);
        }
        return maybeAdapter;
    }

    @SuppressWarnings("unchecked")
    public T getAdapterByBackReferenceId(String backReferenceId) {
        return (T) integrationAdapterProxy.getAdapter(backReferenceId)
                                          .orElseThrow(() -> new IntegrationException(String.format(ERR_MSG_ADAPTER_NOT_FOUND, defaultType)));
    }

    public IntegrationAdapterProxy getIntegrationAdapterProxy() {
        return integrationAdapterProxy;
    }

    public String getDefaultType() {
        return defaultType;
    }
}
