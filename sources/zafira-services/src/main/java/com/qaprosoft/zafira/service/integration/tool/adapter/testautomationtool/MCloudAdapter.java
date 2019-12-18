package com.qaprosoft.zafira.service.integration.tool.adapter.testautomationtool;

import com.qaprosoft.zafira.models.entity.integration.Integration;
import com.qaprosoft.zafira.service.integration.tool.adapter.AbstractIntegrationAdapter;
import com.qaprosoft.zafira.service.integration.tool.adapter.AdapterParam;
import com.qaprosoft.zafira.service.util.UrlUtils;
import kong.unirest.UnirestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.MalformedURLException;

public class MCloudAdapter extends AbstractIntegrationAdapter implements TestAutomationToolAdapter  {

    private final String url;
    private final String username;
    private final String accessKey;

    public MCloudAdapter(Integration integration) {
        super(integration);
        this.url = getAttributeValue(integration, Parameter.URL);
        this.username = getAttributeValue(integration, Parameter.USERNAME);
        this.accessKey = getAttributeValue(integration, Parameter.PASSWORD);
    }

    @Override
    public boolean isConnected() {
        try {
            return UrlUtils.verifyStatusByPath(url, username, accessKey, "/status", false);
        } catch (UnirestException | MalformedURLException e) {
            LOGGER.error("Unable to check MCloud connectivity", e);
            return false;
        }
    }

    @Override
    public String buildUrl() {
        return UrlUtils.buildBasicAuthUrl(url, username, accessKey);
    }

    @Getter
    @AllArgsConstructor
    private enum Parameter implements AdapterParam {
        URL("MCLOUD_URL"),
        USERNAME("MCLOUD_USER"),
        PASSWORD("MCLOUD_PASSWORD");

        private final String name;
    }

}
