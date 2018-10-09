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
package com.qaprosoft.zafira.models.dto;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.qaprosoft.zafira.models.db.Status;
import com.qaprosoft.zafira.models.db.Tag;

@JsonInclude(Include.NON_NULL)
public class TestType extends AbstractType {
	private static final long serialVersionUID = 7777895715362820880L;
	@NotNull
	private String name;
	private Status status;
	private String testArgs;
	@NotNull
	private Long testRunId;
	@NotNull
	private Long testCaseId;
	private String testGroup;
	private String message;
	private Integer messageHashCode;
	private Long startTime;
	private Long finishTime;
	private List<String> workItems;
	private int retry;
	private String configXML;
	private Map<String, Long> testMetrics;
	private boolean knownIssue;
	private boolean blocker;
	private boolean needRerun;
	private String dependsOnMethods;
	private String testClass;
	@Valid
	private Set<TestArtifactType> artifacts = new HashSet<>();
	private String ciTestId;
	@Valid
	private Set<TagType> tags;

	public TestType() {

	}

	public TestType(String name, Status status, String testArgs, Long testRunId, Long testCaseId, Long startTime,
			List<String> workItems, int retry, String configXML) {
		this.name = name;
		this.status = status;
		this.testArgs = testArgs;
		this.testRunId = testRunId;
		this.testCaseId = testCaseId;
		this.startTime = startTime;
		this.workItems = workItems;
		this.retry = retry;
		this.configXML = configXML;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getTestArgs() {
		return testArgs;
	}

	public void setTestArgs(String testArgs) {
		this.testArgs = testArgs;
	}

	public Long getTestRunId() {
		return testRunId;
	}

	public void setTestRunId(Long testRunId) {
		this.testRunId = testRunId;
	}

	public Long getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(Long testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Long finishTime) {
		this.finishTime = finishTime;
	}

	public List<String> getWorkItems() {
		return workItems;
	}

	public void setWorkItems(List<String> workItems) {
		this.workItems = workItems;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public String getConfigXML() {
		return configXML;
	}

	public void setConfigXML(String configXML) {
		this.configXML = configXML;
	}

	public Map<String, Long> getTestMetrics() {
		return testMetrics;
	}

	public void setTestMetrics(Map<String, Long> testMetrics) {
		this.testMetrics = testMetrics;
	}

	public boolean isKnownIssue() {
		return knownIssue;
	}

	public void setKnownIssue(boolean knownIssue) {
		this.knownIssue = knownIssue;
	}

	public boolean isBlocker() {
		return blocker;
	}

	public void setBlocker(boolean blocker) {
		this.blocker = blocker;
	}

	public String getTestGroup() {
		return testGroup;
	}

	public void setTestGroup(String testGroup) {
		this.testGroup = testGroup;
	}

	public boolean isNeedRerun() {
		return needRerun;
	}

	public void setNeedRerun(boolean needRerun) {
		this.needRerun = needRerun;
	}

	public String getDependsOnMethods() {
		return dependsOnMethods;
	}

	public void setDependsOnMethods(String dependsOnMethods) {
		this.dependsOnMethods = dependsOnMethods;
	}

	public Integer getMessageHashCode() {
		return messageHashCode;
	}

	public void setMessageHashCode(Integer messageHashCode) {
		this.messageHashCode = messageHashCode;
	}

	public String getTestClass() {
		return testClass;
	}

	public void setTestClass(String testClass) {
		this.testClass = testClass;
	}

	public Set<TestArtifactType> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(Set<TestArtifactType> artifacts) {
		this.artifacts = artifacts;
	}

	public String getCiTestId() {
		return ciTestId;
	}

	public void setCiTestId(String ciTestId) {
		this.ciTestId = ciTestId;
	}

	public Set<TagType> getTags() {
		return tags;
	}

	public void setTags(Set<TagType> tags) {
		this.tags = tags;
	}
}