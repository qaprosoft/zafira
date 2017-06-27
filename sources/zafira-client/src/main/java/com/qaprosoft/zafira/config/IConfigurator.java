package com.qaprosoft.zafira.config;

import java.util.List;
import java.util.Map;

import org.testng.ISuite;
import org.testng.ITestResult;

import com.qaprosoft.zafira.models.db.TestRun.DriverMode;
import com.qaprosoft.zafira.models.dto.TestArtifactType;
import com.qaprosoft.zafira.models.dto.config.ConfigurationType;

/**
 * Interface provided to perform better integration with Zafira reporting tool.
 * 
 * @author akhursevich
 */
public interface IConfigurator
{
	ConfigurationType getConfiguration();
	
	String getOwner(ISuite suite);
	
	String getOwner(ITestResult test);
	
	String getTestName(ITestResult test);
	
	String getTestMethodName(ITestResult test);
	
	String getLogURL(ITestResult test);
	
	String getDemoURL(ITestResult test);
	
	List<TestArtifactType> getArtifacts(ITestResult test);
	
	List<String> getTestWorkItems(ITestResult test);
	
	int getRunCount(ITestResult test);
	
	DriverMode getDriverMode();
	
	Map<String, Long> getTestMetrics(ITestResult test);
}
