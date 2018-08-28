/*******************************************************************************
 * Copyright 2013-2018 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.zafira.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	String getPrimaryOwner(ITestResult test);
	
	String getSecondaryOwner(ITestResult test);
	
	String getTestName(ITestResult test);
	
	String getTestMethodName(ITestResult test);
	
	Set<TestArtifactType> getArtifacts(ITestResult test);
	
	List<String> getTestWorkItems(ITestResult test);
	
	int getRunCount(ITestResult test);
	
	DriverMode getDriverMode();
	
	Map<String, Long> getTestMetrics(ITestResult test);
	
}
