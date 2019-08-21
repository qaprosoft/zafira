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
package com.qaprosoft.zafira.services.services.application.integration.impl.google.models;

import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.qaprosoft.zafira.models.db.Test;
import com.qaprosoft.zafira.models.db.TestRun;
import com.qaprosoft.zafira.services.services.application.TestRunService;
import com.qaprosoft.zafira.services.services.application.TestService;
import com.qaprosoft.zafira.services.services.application.integration.impl.google.GoogleDriveService;
import com.qaprosoft.zafira.services.services.application.integration.impl.google.GoogleService;
import com.qaprosoft.zafira.services.services.application.integration.impl.google.GoogleSpreadsheetsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.qaprosoft.zafira.services.util.DateTimeUtil.calculateDuration;
import static com.qaprosoft.zafira.services.util.XmlConfigurationUtil.getConfigValueByName;
import static com.qaprosoft.zafira.services.util.XmlConfigurationUtil.isConfigValueIsEmpty;

@Service
public class
TestRunSpreadsheetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunSpreadsheetService.class);

    private static final String TEST_RUN_INFO_SHEET_NAME = "INFO";

    private static final String TEST_RUN_RESULTS_SHEET_NAME = "RESULT";

    @Autowired
    private GoogleService googleService;

    @Autowired
    private TestService testService;

    @Autowired
    private TestRunService testRunService;

    public String createTestRunResultSpreadsheet(TestRun testRun, String... accessRecipients) {
        String result = null;
        GoogleSpreadsheetsService spreadsheetsService = googleService.getSpreadsheetsService();
        GoogleDriveService driveService = googleService.getDriveService();
        try {
            List<List<Object>> testRunInfo = collectTestRunInfo(testRun);
            Spreadsheet spreadsheet = spreadsheetsService.createSpreadsheet(testRun.getTestSuite().getName(), TEST_RUN_INFO_SHEET_NAME);
            result = spreadsheet.getSpreadsheetUrl();
            Sheet infoSheet = spreadsheet.getSheets().get(0);
            String spreadsheetId = spreadsheet.getSpreadsheetId();
            Request infoHeaderCellsStyleRequest = spreadsheetsService.setCellsStyle(infoSheet.getProperties().getSheetId(),
                    0, testRunInfo.size(), 0, 1, true, 10, "Arial", true);
            Request infoCellsStyleRequest = spreadsheetsService.setCellsStyle(infoSheet.getProperties().getSheetId(),
                    1, testRunInfo.size(), 1, testRunInfo.get(0).size(), true, 10, "Arial", false);
            spreadsheetsService.batchUpdate(spreadsheet.getSpreadsheetId(), Arrays.asList(infoHeaderCellsStyleRequest, infoCellsStyleRequest));
            SheetProperties infoSheetProperties = infoSheet.getProperties();
            spreadsheetsService.writeValuesIntoSpreadsheet(spreadsheet.getSpreadsheetId(), testRunInfo, TEST_RUN_INFO_SHEET_NAME);
            Request infoColumnsAndRowsCountRequest = spreadsheetsService.setColumnAndRowsCounts(infoSheetProperties, testRunInfo.get(0).size(),
                    testRunInfo.size());
            Request infoAutoResizeRequest = spreadsheetsService.setAutoResizeDimensionToGrid(spreadsheet, infoSheetProperties.getSheetId());
            Request infoTabColorRequest = spreadsheetsService.setTabColor(infoSheetProperties, 1F, 0, 0);
            spreadsheetsService.batchUpdate(spreadsheet.getSpreadsheetId(),
                    Arrays.asList(infoColumnsAndRowsCountRequest, infoTabColorRequest, infoAutoResizeRequest));

            List<List<Object>> testRunResults = collectTestRunResults(testRun);
            spreadsheetsService.createGrid(spreadsheet.getSpreadsheetId(), TEST_RUN_RESULTS_SHEET_NAME);
            spreadsheet = spreadsheetsService.getSpreadsheetById(spreadsheet.getSpreadsheetId());
            Sheet sheet = spreadsheet.getSheets().stream().filter(s -> s.getProperties().getTitle().equalsIgnoreCase(TEST_RUN_RESULTS_SHEET_NAME))
                    .findFirst().orElse(new Sheet());
            Request cellsStyleRequest = spreadsheetsService.setCellsStyle(sheet.getProperties().getSheetId(),
                    0, 1, 0, testRunResults.get(0).size(), true, 10, "Arial", true);
            spreadsheetsService.batchUpdate(spreadsheet.getSpreadsheetId(), Collections.singletonList(cellsStyleRequest));
            SheetProperties sheetProperties = sheet.getProperties();
            spreadsheetsService.writeValuesIntoSpreadsheet(spreadsheet.getSpreadsheetId(), testRunResults, TEST_RUN_RESULTS_SHEET_NAME);
            Request columnsAndRowsCountRequest = spreadsheetsService.setColumnAndRowsCounts(sheetProperties, testRunResults.get(0).size(),
                    testRunResults.size());
            Request autoResizeRequest = spreadsheetsService.setAutoResizeDimensionToGrid(spreadsheet, sheetProperties.getSheetId());
            Request tabColorRequest = spreadsheetsService.setTabColor(sheetProperties, 1F, 0, 0);
            spreadsheetsService.batchUpdate(spreadsheet.getSpreadsheetId(),
                    Arrays.asList(columnsAndRowsCountRequest, tabColorRequest, autoResizeRequest));
            Arrays.asList(accessRecipients).forEach(recipient -> driveService
                    .shareFile(spreadsheetId, GoogleDriveService.GranteeType.USER,
                            GoogleDriveService.GranteeRoleType.READER, recipient));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public List<List<Object>> collectTestRunResults(final TestRun testRun) {
        List<List<Object>> result = new ArrayList<>();
        result.add(Arrays.asList("Status", "Message", "Title", "Owner", "Secondary owner", "Device", "Elapsed", "Started at", ""));
        List<Test> tests = testService.getTestsByTestRunId(testRun.getId());
        tests.forEach(test -> {
            List<Object> testResult = new ArrayList<>();
            testResult.add(getNotNullSpreadsheetValue(test.getStatus().name()));
            testResult.add(getNotNullSpreadsheetValue(test.getMessage()));
            testResult.add(getNotNullSpreadsheetValue(test.getName()));
            testResult.add(getNotNullSpreadsheetValue(test.getOwner()));
            testResult.add(getNotNullSpreadsheetValue(test.getSecondaryOwner()));
            testResult.add(getNotNullSpreadsheetValue(test.getTestConfig().getDevice()));
            Duration duration = calculateDuration(test.getStartTime(), test.getFinishTime());
            String elapsedTime = (duration.toDays() > 0 ? String.format("%d days", duration.toDays()) : "") +
                    (duration.toHours() > 0 ? String.format(" %d hours", duration.toHours()) : "") +
                    (duration.toMinutes() > 0 ? String.format(" %d minutes", duration.toMinutes()) : "") +
                    (duration.toSeconds() > 0 ? String.format(" %d seconds", duration.toSeconds()) : "");
            testResult.add(getNotNullSpreadsheetValue(elapsedTime));
            testResult.add(getNotNullSpreadsheetValue(new SimpleDateFormat("E, MM/dd/yyyy HH:mm:ss Z").format(test.getStartTime())));
            String hyperLink = "=HYPERLINK(\"%s\", \"%s\")\n";
            StringBuilder hyperLinks = new StringBuilder();
            test.getArtifacts().forEach(artifact -> hyperLinks.append(String.format(hyperLink, artifact.getLink(), artifact.getName())));
            testResult.add(hyperLinks.toString());
            result.add(testResult);
        });
        return result;
    }

    public List<List<Object>> collectTestRunInfo(final TestRun testRun) {
        String[] sideTitles = { "Environment", "Version", "Platform", "Finished", "Elapsed", "Test job URL", "Passed", "Failed | Known | Blockers",
                "Skipped", "Success rate" };
        List<List<Object>> result = new ArrayList<>();
        Arrays.asList(sideTitles).forEach(title -> {
            List<Object> testResult = new ArrayList<>();
            testResult.add(title);
            String hyperLink = "=HYPERLINK(\"%s\", \"%s\")\n";
            switch (title) {
            case "Environment":
                String environment = getConfigValueByName("url", testRun.getConfigXML());
                testResult.add(testRun.getEnv() != null ? String.format("%s - %s", getNotNullSpreadsheetValue(testRun.getEnv()), environment) : "");
                break;
            case "Version":
                testResult.add(getNotNullSpreadsheetValue(getConfigValueByName("app_version", testRun.getConfigXML())));
                break;
            case "Platform":
                String platform = getConfigValueByName("platform", testRun.getConfigXML());
                String mobileDeviceName = getConfigValueByName("mobile_device_name", testRun.getConfigXML());
                String mobilePlatformName = getConfigValueByName("mobile_platform_name", testRun.getConfigXML());
                String mobilePlatformVersion = getConfigValueByName("mobile_platform_version", testRun.getConfigXML());
                String browser = getConfigValueByName("browser", testRun.getConfigXML());
                String browserVersion = getConfigValueByName("browser_version", testRun.getConfigXML());
                testResult.add(getNotNullSpreadsheetValue(
                        (!isConfigValueIsEmpty(platform) ? platform + "\n" : "") +
                                (!isConfigValueIsEmpty(mobileDeviceName) ? mobileDeviceName + " - " : "") +
                                (!isConfigValueIsEmpty(mobilePlatformName) ? mobilePlatformName + "\n" : "") +
                                (!isConfigValueIsEmpty(mobilePlatformVersion) ? mobilePlatformVersion + "\n" : "") +
                                (!isConfigValueIsEmpty(platform) && !platform.equalsIgnoreCase("api") ? browser + "\n" : "") +
                                (!isConfigValueIsEmpty(platform) && !platform.equalsIgnoreCase("api") ? browserVersion + "\n" : "")));
                break;
            case "Finished":
                testResult.add(getNotNullSpreadsheetValue(new SimpleDateFormat("E, MM/dd/yyyy HH:mm:ss Z").format(testRun.getModifiedAt())));
                break;
            case "Elapsed":
                testResult.add(getElapsed(testRun));
                break;
            case "Test job URL":
                String zafiraServiceUrl = getConfigValueByName("zafira_service_url", testRun.getConfigXML());
                if (!StringUtils.isBlank(zafiraServiceUrl) && !zafiraServiceUrl.equalsIgnoreCase("null")) {
                    testResult.add(
                            String.format("%s\n%s",
                                    String.format(hyperLink, String.format("%s/tests/runs/%d",
                                            getNotNullSpreadsheetValue(getConfigValueByName("zafira_service_url", testRun.getConfigXML())),
                                            testRun.getId()), "Zafira"),
                                    String.format(hyperLink,
                                            String.format("%s/%s/eTAF_Report", testRun.getJob().getJobURL(), testRun.getBuildNumber()), "Jenkins")));
                }
                break;
            case "Passed":
                testResult.add(testRun.getPassed());
                break;
            case "Failed | Known | Blockers":
                testResult.add(testRun.getFailed() + " | " + testRun.getFailedAsKnown() + " | " + testRun.getFailedAsBlocker());
                break;
            case "Skipped":
                testResult.add(testRun.getSkipped());
                break;
            case "Success rate":
                testResult.add(TestRunService.calculateSuccessRate(testRun));
                break;
            default:
                break;
            }
            result.add(testResult);
        });
        return result;
    }

    private Object getNotNullSpreadsheetValue(String value) {
        return value != null ? !value.equalsIgnoreCase("null") ? value : "" : "";
    }

    private String getElapsed(TestRun testRun) {
        return testRun.getElapsed() != null ? LocalTime.ofSecondOfDay(testRun.getElapsed()).toString() : null;
    }
}
