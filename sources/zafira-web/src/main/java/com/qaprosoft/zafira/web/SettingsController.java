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
 ******************************************************************************/

package com.qaprosoft.zafira.web;

import com.qaprosoft.zafira.models.db.Setting;
import com.qaprosoft.zafira.models.dto.aws.SessionCredentials;
import com.qaprosoft.zafira.models.entity.integration.Integration;
import com.qaprosoft.zafira.service.CryptoService;
import com.qaprosoft.zafira.service.ElasticsearchService;
import com.qaprosoft.zafira.service.SettingsService;
import com.qaprosoft.zafira.service.integration.IntegrationService;
import com.qaprosoft.zafira.service.integration.tool.impl.StorageProviderService;
import com.qaprosoft.zafira.web.documented.SettingDocumentedController;
import com.qaprosoft.zafira.web.util.swagger.ApiResponseStatuses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(path = "api/settings", produces = MediaType.APPLICATION_JSON_VALUE)
public class SettingsController extends AbstractController implements SettingDocumentedController {

    private final SettingsService settingsService;
    private final CryptoService cryptoService;
    private final ElasticsearchService elasticsearchService;
    private final IntegrationService integrationService;

    private final StorageProviderService storageProviderService;

    public SettingsController(
            SettingsService settingsService,
            CryptoService cryptoService,
            ElasticsearchService elasticsearchService,
            IntegrationService integrationService,
            StorageProviderService storageProviderService
    ) {
        this.settingsService = settingsService;
        this.cryptoService = cryptoService;
        this.elasticsearchService = elasticsearchService;
        this.integrationService = integrationService;
        this.storageProviderService = storageProviderService;
    }

    @GetMapping("tool/{tool}")
    @Override
    public List<Setting> getSettingsByTool(@PathVariable("tool") String typeName) {
        // TODO by nsidorevich on 2019-10-09: refactor and remove
        List<Setting> settings;
        switch (typeName.toUpperCase()) {
            case "ELASTICSEARCH":
                settings = elasticsearchService.getSettings();
                break;
            case "RABBITMQ":
                settings = collectDecryptedIntegrationSettings("RABBITMQ");
                break;
            case "ZEBRUNNER":
                settings = collectDecryptedIntegrationSettings("ZEBRUNNER");
                break;
            default:
                throw new RuntimeException(String.format("Unsupported tool %s, this API should not be used for anything but ElasticSearch or Rabbit", typeName));
        }
        return settings;
    }

    private List<Setting> collectDecryptedIntegrationSettings(String integrationTypeName) {
        Integration integration = integrationService.retrieveDefaultByIntegrationTypeName(integrationTypeName);
        List<Setting> settings =  integration.getSettings()
                                              .stream()
                                              .map(setting -> {
                                                  if (setting.isEncrypted()) {
                                                      String decryptedValue = cryptoService.decrypt(setting.getValue());
                                                      setting.setValue(decryptedValue);
                                                      setting.setEncrypted(false);
                                                  }
                                                  return new Setting(setting.getParam().getName(), setting.getValue());
                                              })
                                              .collect(Collectors.toList());
        settings.add(new Setting(integrationTypeName + "_ENABLED", Boolean.toString(integration.isEnabled())));
        return settings;
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Updates a setting", nickname = "setting", httpMethod = "PUT", response = Setting.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping
    public Setting updateSetting(@RequestBody Setting setting) {
        return settingsService.updateSetting(setting);
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Retrieves a company logo URL", nickname = "getSettingValue", httpMethod = "GET", response = Setting.class)
    @GetMapping("companyLogo")
    public Setting getCompanyLogoURL() {
        return settingsService.getSettingByName("COMPANY_LOGO_URL");
    }

    // TODO by nsidorevich on 2019-10-09: remove this crap

    @ApiOperation(value = "Receives Amazon session credentials", nickname = "getSessionCredentials", httpMethod = "GET", response = SessionCredentials.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @GetMapping("amazon/creds")
    public SessionCredentials getSessionCredentials() {
        return storageProviderService.getTemporarySessionCredentials().orElse(null);
    }

}
