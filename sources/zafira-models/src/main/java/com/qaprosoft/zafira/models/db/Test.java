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
package com.qaprosoft.zafira.models.db;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Test extends AbstractEntity implements Comparable<Test>
{
	private static final long serialVersionUID = -915700504693067056L;

	private String name;
	private Status status;
	private String testArgs;
	private Long testRunId;
	private Long testCaseId;
    private String testGroup;
	private String message;
	private Integer messageHashCode;
	private Date startTime;
	private Date finishTime;
	private int retry;
	private TestConfig testConfig;
	private List<WorkItem> workItems;
	private boolean knownIssue;
	private boolean blocker;
	private boolean needRerun;
	private String owner;
	private String secondaryOwner;
	private String dependsOnMethods;
	private String testClass;
	private Set<TestArtifact> artifacts = new HashSet<>();

	public Test()
	{
		testConfig = new TestConfig();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getTestArgs()
	{
		return testArgs;
	}

	public void setTestArgs(String testArgs)
	{
		this.testArgs = testArgs;
	}

	public Long getTestRunId()
	{
		return testRunId;
	}

	public void setTestRunId(Long testRunId)
	{
		this.testRunId = testRunId;
	}

	public Long getTestCaseId()
	{
		return testCaseId;
	}

	public void setTestCaseId(Long testCaseId)
	{
		this.testCaseId = testCaseId;
	}

    public String getTestGroup() {
        return testGroup;
    }

    public String getNotNullTestGroup() {
        return testGroup == null? "n/a": testGroup;
    }

    public void setTestGroup(String testGroup) {
        this.testGroup = testGroup;
    }

    public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public Date getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	public Date getFinishTime()
	{
		return finishTime;
	}

	public void setFinishTime(Date finishTime)
	{
		this.finishTime = finishTime;
	}

	public int getRetry()
	{
		return retry;
	}

	public void setRetry(int retry)
	{
		this.retry = retry;
	}

	public TestConfig getTestConfig()
	{
		return testConfig;
	}

	public void setTestConfig(TestConfig testConfig)
	{
		this.testConfig = testConfig;
	}

	public List<WorkItem> getWorkItems()
	{
		return workItems;
	}

	public WorkItem getWorkItemByType(WorkItem.Type type)
	{
		if (workItems != null)
		{
			for (WorkItem workItem : workItems)
			{
				if (type.equals(workItem.getType()))
				{
					return workItem;
				}
			}
		}
		return null;
	}

	public void setWorkItems(List<WorkItem> workItems)
	{
		this.workItems = workItems;
	}

	public boolean isKnownIssue()
	{
		return knownIssue;
	}

	public void setKnownIssue(boolean knownIssue)
	{
		this.knownIssue = knownIssue;
	}

	public boolean isBlocker() {
		return blocker;
	}

	public void setBlocker(boolean blocker) {
		this.blocker = blocker;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getSecondaryOwner() {
		return secondaryOwner;
	}

	public void setSecondaryOwner(String secondaryOwner) {
		this.secondaryOwner = secondaryOwner;
	}

	public boolean isNeedRerun()
	{
		return needRerun;
	}

	public void setNeedRerun(boolean needRerun)
	{
		this.needRerun = needRerun;
	}

	public String getDependsOnMethods()
	{
		return dependsOnMethods;
	}

	public void setDependsOnMethods(String dependsOnMethods)
	{
		this.dependsOnMethods = dependsOnMethods;
	}
	
	public Integer getMessageHashCode()
	{
		return messageHashCode;
	}

	public void setMessageHashCode(Integer messageHashCode)
	{
		this.messageHashCode = messageHashCode;
	}

	public String getTestClass()
	{
		return testClass;
	}

	public void setTestClass(String testClass)
	{
		this.testClass = testClass;
	}
	
	public Set<TestArtifact> getArtifacts()
	{
		return artifacts;
	}

	public void setArtifacts(Set<TestArtifact> artifacts)
	{
		this.artifacts = artifacts;
	}

	@Override
	public int compareTo(Test test)
	{
		if(Arrays.asList(Status.QUEUED, Status.ABORTED, Status.SKIPPED, Status.FAILED).contains(this.getStatus()))
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
}