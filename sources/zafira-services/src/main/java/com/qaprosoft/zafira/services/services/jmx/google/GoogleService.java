package com.qaprosoft.zafira.services.services.jmx.google;

import com.qaprosoft.zafira.models.db.Setting;
import com.qaprosoft.zafira.services.services.SettingsService;
import com.qaprosoft.zafira.services.services.jmx.IJMXService;
import com.qaprosoft.zafira.services.services.jmx.google.auth.GoogleDriveAuthService;
import com.qaprosoft.zafira.services.services.jmx.google.auth.GoogleSheetsAuthService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@ManagedResource(objectName = "bean:name=googleService", description = "Google init Managed Bean",
		currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200)
public class GoogleService implements IJMXService
{

	private static final Logger LOGGER = Logger.getLogger(GoogleService.class);

	private static final String CLIENT_SECRET_JSON = "./client_secret.json";

	@Autowired
	private GoogleDriveAuthService driveAuthService;

	@Autowired
	private GoogleSheetsAuthService sheetsAuthService;

	private GoogleDriveService driveService;

	private GoogleSpreadsheetsService spreadsheetsService;

	@Autowired
	private SettingsService settingsService;

	@Override
	@PostConstruct
	@ManagedOperation(description = "Google initialization")
	public void init()
	{
		driveService = new GoogleDriveService();
		spreadsheetsService = new GoogleSpreadsheetsService();
	}

	@Override
	@SuppressWarnings("all")
	public boolean isConnected()
	{
		boolean result = false;
		try
		{
			File file = new File(CLIENT_SECRET_JSON);
			if(file.exists())
			{
				driveAuthService.getService().about();
				sheetsAuthService.getService().spreadsheets();
				result = true;
			}
		} catch(Exception e)
		{
		}
		return result;
	}

	public File createCredentialsFile(InputStream inputStream, String originalFilename)
	{
		File json = null;
		try
		{
			json = new File(CLIENT_SECRET_JSON);
			Files.deleteIfExists(json.toPath());
			FileUtils.copyInputStreamToFile(inputStream, json);
			Setting originalFilenameSetting = settingsService.getSettingByType(Setting.SettingType.GOOGLE_CLIENT_SECRET_ORIGIN);
			originalFilenameSetting.setValue(originalFilename);
			settingsService.updateSetting(originalFilenameSetting);
			init();
		} catch (Exception e)
		{
			LOGGER.error(e);
		} finally
		{
			IOUtils.closeQuietly(inputStream);
		}
		return json;
	}

	@ManagedAttribute(description = "Get google drive client")
	public GoogleDriveService getDriveService()
	{
		return driveService;
	}

	@ManagedAttribute(description = "Get google spreadsheet client")
	public GoogleSpreadsheetsService getSpreadsheetsService()
	{
		return spreadsheetsService;
	}
}
