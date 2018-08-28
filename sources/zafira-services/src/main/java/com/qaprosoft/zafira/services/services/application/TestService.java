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
package com.qaprosoft.zafira.services.services.application;

import static com.qaprosoft.zafira.models.dto.TestRunStatistics.Action.MARK_AS_BLOCKER;
import static com.qaprosoft.zafira.models.dto.TestRunStatistics.Action.MARK_AS_KNOWN_ISSUE;
import static com.qaprosoft.zafira.models.dto.TestRunStatistics.Action.REMOVE_BLOCKER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qaprosoft.zafira.dbaccess.dao.mysql.application.TestMapper;
import com.qaprosoft.zafira.dbaccess.dao.mysql.application.search.SearchResult;
import com.qaprosoft.zafira.dbaccess.dao.mysql.application.search.TestCaseSearchCriteria;
import com.qaprosoft.zafira.dbaccess.dao.mysql.application.search.TestSearchCriteria;
import com.qaprosoft.zafira.models.db.Status;
import com.qaprosoft.zafira.models.db.Test;
import com.qaprosoft.zafira.models.db.TestArtifact;
import com.qaprosoft.zafira.models.db.TestCase;
import com.qaprosoft.zafira.models.db.TestConfig;
import com.qaprosoft.zafira.models.db.TestRun;
import com.qaprosoft.zafira.models.db.TestRun.DriverMode;
import com.qaprosoft.zafira.models.db.WorkItem;
import com.qaprosoft.zafira.models.db.WorkItem.Type;
import com.qaprosoft.zafira.models.dto.TestRunStatistics;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.exceptions.TestNotFoundException;
import com.qaprosoft.zafira.services.services.application.jmx.JiraService;

import net.rcarz.jiraclient.Issue;

@Service
public class TestService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);

	private static final String INV_COUNT = "InvCount";

	private static final String SPACE = " ";

	private static final List<String> SELENIUM_ERRORS = Arrays.asList("org.openqa.selenium.remote.UnreachableBrowserException", "org.openqa.selenium.TimeoutException", "Session");

	@Autowired
	private TestMapper testMapper;

	@Autowired
	private WorkItemService workItemService;

	@Autowired
	private TestConfigService testConfigService;

	@Autowired
	private TestCaseService testCaseService;

	@Autowired
	private TestRunService testRunService;

	@Autowired
	private JiraService jiraService;

	@Autowired
	private TestArtifactService testArtifactService;

	@Transactional(rollbackFor = Exception.class)
	public Test startTest(Test test, List<String> jiraIds, String configXML) throws ServiceException
	{
		// New or Queued test
		if ((test.getId() == null || test.getId() == 0) || test.getStatus() == Status.QUEUED)
		{
			//This code block is executed only for the first job run
			TestConfig config = testConfigService.createTestConfigForTest(test, configXML);
			test.setTestConfig(config);
			test.setStatus(Status.IN_PROGRESS);

			if((test.getId() == null || test.getId() == 0))
			{
				testMapper.createTest(test);
				if (jiraIds != null)
				{
					for (String jiraId : jiraIds)
					{
						if (!StringUtils.isEmpty(jiraId))
						{
							WorkItem workItem = workItemService.createOrGetWorkItem(new WorkItem(jiraId));
							testMapper.createTestWorkItem(test, workItem);
						}
					}
				}
			}
			else
			{
				updateTest(test);
			}
			testRunService.updateStatistics(test.getTestRunId(), test.getStatus());
		}
		// Existing test
		else
		{
			testRunService.updateStatistics(test.getTestRunId(), test.getStatus(), true);
			test.setMessage(null);
			test.setFinishTime(null);
			test.setStatus(Status.IN_PROGRESS);
			test.setKnownIssue(false);
			test.setBlocker(false);
			updateTest(test);
			workItemService.deleteKnownIssuesByTestId(test.getId());
			testArtifactService.deleteTestArtifactsByTestId(test.getId());
		}
		return test;
	}

	@Transactional(rollbackFor = Exception.class)
	public void createQueuedTest(Test test, Long queuedTestRunId) throws ServiceException
	{
		test.setId(null);
		test.setTestRunId(queuedTestRunId);
		test.setStatus(Status.QUEUED);
		test.setTestConfig(null);
		test.setNeedRerun(false);
		testMapper.createTest(test);
	}

	@Transactional(rollbackFor = Exception.class)
	public void updateQueuedTest(Test test, Long testRunId) throws ServiceException
	{
		test.setTestRunId(testRunId);
		testMapper.updateTest(test);
	}

	@Transactional(rollbackFor = Exception.class)
	public Test finishTest(Test test, String configXML) throws ServiceException
	{
		Test existingTest = getNotNullTestById(test.getId());

		existingTest.setFinishTime(test.getFinishTime());
		existingTest.setStatus(test.getStatus());
		existingTest.setRetry(test.getRetry());
		existingTest.setTestConfig(testConfigService.createTestConfigForTest(test, configXML));

		// Wrap all additional test finalization logic to make sure status saved
		try
		{
			String message = test.getMessage();
			if (message != null)
			{
				existingTest.setMessage(message);
				// Handling of known Selenium errors
				for (String error : SELENIUM_ERRORS)
				{
					if (message.startsWith(error))
					{
						message = error;
						break;
					}
				}
				existingTest.setMessageHashCode(getTestMessageHashCode(message));
			}
			
			// Resolve known issues
			if (Status.FAILED.equals(test.getStatus()))
			{
				WorkItem knownIssue = workItemService.getWorkItemByTestCaseIdAndHashCode(existingTest.getTestCaseId(), getTestMessageHashCode(test.getMessage()));
				if (knownIssue != null)
				{
					Issue issueFromJira = jiraService.getIssue(knownIssue.getJiraId());
					boolean isJiraIdClosed = jiraService.isConnected() && issueFromJira != null
							&& jiraService.isIssueClosed(issueFromJira);
					if (!isJiraIdClosed)
					{
						existingTest.setKnownIssue(true);
						existingTest.setBlocker(knownIssue.isBlocker());
						testRunService.updateStatistics(test.getTestRunId(), MARK_AS_KNOWN_ISSUE);
						if (existingTest.isBlocker())
						{
							testRunService.updateStatistics(test.getTestRunId(), TestRunStatistics.Action.MARK_AS_BLOCKER);
						}
						testMapper.createTestWorkItem(existingTest, knownIssue);
						if (existingTest.getWorkItems() == null)
						{
							existingTest.setWorkItems(new ArrayList<WorkItem>());
						}
						existingTest.getWorkItems().add(knownIssue);
					}
				}
			}

			// Save artifacts
			if (!CollectionUtils.isEmpty(test.getArtifacts()))
			{
				existingTest.setArtifacts(new HashSet<>());
				test.getArtifacts().stream().filter(TestArtifact::isValid).forEach(artifact -> {
					artifact.setTestId(test.getId());
					existingTest.getArtifacts().add(artifact);
					testArtifactService.createOrUpdateTestArtifact(artifact);
				});
			}
			
			TestCase testCase = testCaseService.getTestCaseById(test.getTestCaseId());
			if (testCase != null)
			{
				testCase.setStatus(test.getStatus());
				testCaseService.updateTestCase(testCase);
			}
			
		} 
		catch (Exception e) 
		{
			LOGGER.error("Test finalization error: " + e.getMessage());
		}
		finally 
		{
			testMapper.updateTest(existingTest);
			testRunService.updateStatistics(existingTest.getTestRunId(), existingTest.getStatus());
		}
		return existingTest;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Test skipTest(Test test) throws ServiceException
	{
		test.setStatus(Status.SKIPPED);
		testRunService.updateStatistics(test.getTestRunId(), Status.SKIPPED);
		updateTest(test);
		return test;
	}

	@Transactional(rollbackFor = Exception.class)
	public Test abortTest(Test test, String abortCause) throws ServiceException
	{
		test.setStatus(Status.ABORTED);
		test.setMessage(abortCause);
		testRunService.updateStatistics(test.getTestRunId(), Status.ABORTED);
		updateTest(test);
		return test;
	}

	@Transactional(rollbackFor = Exception.class)
	public Test changeTestStatus(long id, Status newStatus) throws ServiceException, InterruptedException
	{
		Test test = getTestById(id);
		if (test == null)
		{
			throw new TestNotFoundException();
		}
		testRunService.updateStatistics(test.getTestRunId(), newStatus, test.getStatus());
		test.setStatus(newStatus);
		updateTest(test);
		TestCase testCase = testCaseService.getTestCaseById(test.getTestCaseId());
		if (testCase != null)
		{
			testCase.setStatus(test.getStatus());
			testCaseService.updateTestCase(testCase);
		}
		testRunService.calculateTestRunResult(test.getTestRunId(), false);
		return test;
	}

	@Transactional(rollbackFor = Exception.class)
	public Test createTestWorkItems(long id, List<String> jiraIds) throws ServiceException
	{
		Test test = getTestById(id);
		if (test == null)
		{
			throw new ServiceException("Test not found by id: " + id);
		}
		for (String jiraId : jiraIds)
		{
			if (!StringUtils.isEmpty(jiraId))
			{
				WorkItem workItem = workItemService.createOrGetWorkItem(new WorkItem(jiraId));
				testMapper.createTestWorkItem(test, workItem);
			}

		}
		return test;
	}

	@Transactional(readOnly = true)
	public Test getTestById(long id) throws ServiceException
	{
		return testMapper.getTestById(id);
	}

	@Transactional(readOnly = true)
	public Test getNotNullTestById(long id) throws ServiceException
	{
		Test test = getTestById(id);
		if (test == null)
		{
			throw new TestNotFoundException("Test ID: " + id);
		}
		return test;
	}

	@Transactional(readOnly = true)
	public List<Test> getTestsByTestRunId(long testRunId) throws ServiceException
	{
		return testMapper.getTestsByTestRunId(testRunId);
	}

	@Transactional(readOnly = true)
	public List<Test> getTestsByTestRunId(String testRunId) throws ServiceException
	{
		return testRunId.matches("\\d+") ? testMapper.getTestsByTestRunId(Long.valueOf(testRunId)) : testMapper.getTestsByTestRunCiRunId(testRunId);
	}

	@Transactional(readOnly = true)
	public List<Test> getTestsByTestRunCiRunId(String testRunCiRunId) throws ServiceException
	{
		return testMapper.getTestsByTestRunCiRunId(testRunCiRunId);
	}

	@Transactional(readOnly = true)
	public List<Test> getTestsByTestRunIdAndStatus(long testRunId, Status status) throws ServiceException
	{
		return testMapper.getTestsByTestRunIdAndStatus(testRunId, status);
	}

	@Transactional(readOnly = true)
	public List<Test> getTestsByWorkItemId(long workItemId) throws ServiceException
	{
		return testMapper.getTestsByWorkItemId(workItemId);
	}

	@Transactional(rollbackFor = Exception.class)
	public Test updateTest(Test test) throws ServiceException
	{
		testMapper.updateTest(test);
		return test;
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteTest(Test test) throws ServiceException
	{
		testMapper.deleteTest(test);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteTestById(long id) throws ServiceException
	{
		testMapper.deleteTestById(id);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteQueuedTest(Test test) throws ServiceException
	{
		testMapper.deleteTestByTestRunIdAndNameAndStatus(test.getTestRunId(), test.getName(), Status.QUEUED);
	}

	@Transactional(readOnly = true)
	public SearchResult<Test> searchTests(TestSearchCriteria sc) throws ServiceException
	{
		SearchResult<Test> results = new SearchResult<>();
		results.setPage(sc.getPage());
		results.setPageSize(sc.getPageSize());
		results.setSortOrder(sc.getSortOrder());
		List<Test> tests = testMapper.searchTests(sc);
		for (Test test : tests)
		{
			test.setArtifacts(new TreeSet<>(test.getArtifacts()));
		}
		results.setResults(tests);
		results.setTotalResults(testMapper.getTestsSearchCount(sc));
		return results;
	}

	@Transactional(rollbackFor = Exception.class)
	public WorkItem createOrUpdateTestWorkItem(long testId, WorkItem workItem) throws ServiceException, InterruptedException
	{
		Test test = getNotNullTestById(testId);
		Type workItemType = workItem.getType();
		WorkItem attachedWorkItem = test.getWorkItemByType(workItemType);

		if (workItemType == Type.BUG) {
			workItem.setHashCode(getTestMessageHashCode(test.getMessage()));
			if (!test.isKnownIssue())
				testRunService.updateStatistics(test.getTestRunId(), MARK_AS_KNOWN_ISSUE);
			if (!test.isBlocker() && workItem.isBlocker())
				testRunService.updateStatistics(test.getTestRunId(), MARK_AS_BLOCKER);
			else if (test.isBlocker() && !workItem.isBlocker())
				testRunService.updateStatistics(test.getTestRunId(), REMOVE_BLOCKER);

			test.setKnownIssue(true);
			test.setBlocker(workItem.isBlocker());
			updateTest(test);
		}

		if (workItem.getId() != null && attachedWorkItem == null)
		{
			workItemService.updateWorkItem(workItem);
			testMapper.createTestWorkItem(test, workItem);
		}
		else if (workItem.getId() != null && attachedWorkItem != null)
		{
			if (workItemType == Type.BUG) {
				// Generate random hashcode to unlink known issue
				attachedWorkItem.setHashCode(RandomUtils.nextInt());
				workItemService.updateWorkItem(attachedWorkItem);
			}
			workItemService.updateWorkItem(workItem);
			deleteTestWorkItemByTestIdAndWorkItemType(testId, workItemType);
			testMapper.createTestWorkItem(test, workItem);
		}
		else
		{
			workItemService.createWorkItem(workItem);
			deleteTestWorkItemByTestIdAndWorkItemType(testId, workItemType);
			testMapper.createTestWorkItem(test, workItem);
		}
		testRunService.calculateTestRunResult(test.getTestRunId(), false);

		return workItemService.getWorkItemById(workItem.getId());
	}

	@Transactional(rollbackFor = Exception.class)
	public WorkItem createWorkItem(long testId, WorkItem workItem) throws ServiceException, InterruptedException
	{
		Test test = getNotNullTestById(testId);
		workItemService.createWorkItem(workItem);
		testMapper.createTestWorkItem(test, workItem);
		return workItemService.getWorkItemByJiraIdAndType(workItem.getJiraId(), workItem.getType());
	}

	@Transactional(rollbackFor = Exception.class)
	public TestRun deleteTestWorkItemByWorkItemIdAndTest(long workItemId, Test test) throws ServiceException, InterruptedException
	{
		test.setKnownIssue(false);
		test.setBlocker(false);
		updateTest(test);

		WorkItem workItem = workItemService.getWorkItemById(workItemId);
		// Generate random hashcode to unlink known issue
		if (workItem.getType() == Type.BUG) {
			workItem.setHashCode(RandomUtils.nextInt());
		}
		workItemService.updateWorkItem(workItem);
		deleteTestWorkItemByWorkItemIdAndTestId(workItemId, test.getId());

		testRunService.calculateTestRunResult(test.getTestRunId(), false);
		TestRun testRun = testRunService.getTestRunById(test.getTestRunId());

		return testRun;
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteTestWorkItemByWorkItemIdAndTestId(long workItemId, long testId) throws ServiceException, InterruptedException
	{
		testMapper.deleteTestWorkItemByWorkItemIdAndTestId(workItemId, testId);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteTestWorkItemByTestIdAndWorkItemType(long testId, Type type) throws ServiceException, InterruptedException
	{
		testMapper.deleteTestWorkItemByTestIdAndWorkItemType(testId, type);
	}

	@Transactional
	public void updateTestRerunFlags(TestRun testRun, List<Test> tests)
	{
		List<Long> testIds = getTestIds(tests);
		testMapper.updateTestsNeedRerun(testIds, false);

		try
		{
			// In case of SUITE_MODE we are rerunning all tests if test run status not PASSED
			if (DriverMode.SUITE_MODE.equals(testRun.getDriverMode()))
			{
				if (!Status.PASSED.equals(testRun.getStatus()))
				{
					testMapper.updateTestsNeedRerun(testIds, true);
				}
			}
			else
			{
				// Look #Test implements comparable so that all SKIPPED and FAILED tests go first
				Collections.sort(tests);

				TestCaseSearchCriteria sc = new TestCaseSearchCriteria();
				sc.setPageSize(Integer.MAX_VALUE);
				for (Test test : tests)
				{
					sc.addId(test.getTestCaseId());
				}

				Map<Long, TestCase> testCasesById = new HashMap<>();
				Map<String, List<Long>> testCasesByClass = new HashMap<>();
				Map<String, List<Long>> testCasesByMethod = new HashMap<>();
				Set<Long> testCasesToRerun = new HashSet<>();

				for (TestCase tc : testCaseService.searchTestCases(sc).getResults())
				{
					testCasesById.put(tc.getId(), tc);

					if (!testCasesByClass.containsKey(tc.getTestClass()))
					{
						testCasesByClass.put(tc.getTestClass(), new ArrayList<Long>());
					}
					testCasesByClass.get(tc.getTestClass()).add(tc.getId());

					if (!testCasesByMethod.containsKey(tc.getTestMethod()))
					{
						testCasesByMethod.put(tc.getTestMethod(), new ArrayList<Long>());
					}
					testCasesByMethod.get(tc.getTestMethod()).add(tc.getId());
				}

				for (Test test : tests)
				{

					if ((Arrays.asList(Status.FAILED, Status.SKIPPED).contains(test.getStatus()) && !test.isKnownIssue())
							|| test.getStatus().equals(Status.ABORTED)
							|| test.getStatus().equals(Status.QUEUED))
					{
						switch (testRun.getDriverMode())
						{
						case SUITE_MODE:
							// Do nothing
							break;
						case CLASS_MODE:
							String className = testCasesById.get(test.getTestCaseId()).getTestClass();
							testCasesToRerun.addAll(testCasesByClass.get(className));
							break;

						case METHOD_MODE:
							String methodName = testCasesById.get(test.getTestCaseId()).getTestMethod();

							if (test.getName().contains(INV_COUNT))
							{
								testCasesToRerun.addAll(testCasesByMethod.get(methodName));
							}
							else
							{
								testMapper.updateTestsNeedRerun(Collections.singletonList(test.getId()), true);
							}

							if (!StringUtils.isEmpty(test.getDependsOnMethods()))
							{
								for (String method : test.getDependsOnMethods().split(SPACE))
								{
									testCasesToRerun.addAll(testCasesByMethod.get(method));
								}
							}
							break;
						}
					}
				}

				testIds = new ArrayList<>();
				for (Test test : tests)
				{
					if (testCasesToRerun.contains(test.getTestCaseId()))
					{
						testIds.add(test.getId());
					}
				}
				testMapper.updateTestsNeedRerun(testIds, true);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Unable to calculate rurun flags", e);
			testMapper.updateTestsNeedRerun(testIds, true);
		}
	}

	public int getTestMessageHashCode(String message)
	{
		return message != null ? message.replaceAll("\\d+", "*").replaceAll("\\[.*\\]", "*").hashCode() : 0;
	}

	public List<Long> getTestIds(List<Test> tests)
	{
		List<Long> testIds = new ArrayList<>();
		for (Test test : tests)
		{
			testIds.add(test.getId());
		}
		return testIds;
	}
}
