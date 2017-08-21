package com.qaprosoft.zafira.dbaccess.dao.mysql;

import java.util.List;

import com.qaprosoft.zafira.models.db.Setting;
import org.apache.ibatis.annotations.Param;

public interface SettingsMapper
{
	void createSetting(Setting setting);

	Setting getSettingById(long id);

	Setting getSettingByName(String name);

	List<Setting> getSettingsByTool (String tool);

	List<Setting> getSettingsByEncrypted (boolean isEncrypted);

	List<Setting> getAllSettings();

	List<Setting> getSettingsByIntegration(@Param("isIntegrationTool") boolean isIntegrationTool);

	List<String> getTools();

	void updateSetting(Setting setting);

	void deleteSetting(Setting setting);

	void deleteSettingById(long id);
}
