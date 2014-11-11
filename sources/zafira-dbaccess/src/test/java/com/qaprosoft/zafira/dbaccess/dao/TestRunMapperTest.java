package com.qaprosoft.zafira.dbaccess.dao;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.qaprosoft.zafira.dbaccess.dao.mysql.TestRunMapper;
import com.qaprosoft.zafira.dbaccess.model.Job;
import com.qaprosoft.zafira.dbaccess.model.TestRun;
import com.qaprosoft.zafira.dbaccess.model.TestRun.Initiator;
import com.qaprosoft.zafira.dbaccess.model.TestRun.Status;
import com.qaprosoft.zafira.dbaccess.model.User;

@Test
@ContextConfiguration("classpath:com/qaprosoft/zafira/dbaccess/dbaccess-test.xml")
public class TestRunMapperTest extends AbstractTestNGSpringContextTests
{
	/**
	 * Turn this on to enable this test
	 */
	private static final boolean ENABLED = true;
	
	private static final TestRun TEST_RUN = new TestRun()
	{
		private static final long serialVersionUID = 1L;
		{
			User user = new User();
			user.setId(1L);
			
			Job job = new Job();
			job.setId(1L);
			
			setUser(user);
			setTestSuiteId(1L);
			setScmBranch("prod");
			setScmRevision("sdfsdfsdfs234234132ff");
			setScmURL("http://localhost:8080/lc");
			setJob(job);
//			setUpstreamJob(job);
			setUpstreamJobBuildNumber(2);
			setConfigXML("<xml>");
			setBuildNumber(5);
			setStatus(Status.PASSED);
			setStartedBy(Initiator.HUMAN);
			setWorkItemId(1L);
		}
	};

	@Autowired
	private TestRunMapper testRunMapper;

	@Test(enabled = ENABLED)
	public void createTestRun()
	{
		testRunMapper.createTestRun(TEST_RUN);

		assertNotEquals(TEST_RUN.getId(), 0, "TestRun ID must be set up by autogenerated keys");
	}

	@Test(enabled = ENABLED, dependsOnMethods =
	{ "createTestRun" })
	public void getTestRunById()
	{
		checkTestRun(testRunMapper.getTestRunById(TEST_RUN.getId()));
	}

	@Test(enabled = ENABLED, dependsOnMethods =
	{ "createTestRun" })
	public void updateTestRun()
	{
		TEST_RUN.getUser().setId(2L);
		TEST_RUN.setTestSuiteId(2L);
		TEST_RUN.setScmBranch("stg");
		TEST_RUN.setScmRevision("sdfsdsdffs4132ff");
		TEST_RUN.setScmURL("http://localhost:8080/lc2");
		TEST_RUN.getJob().setId(2L);
		TEST_RUN.getUpstreamJob().setId(2L);
		TEST_RUN.setUpstreamJobBuildNumber(5);
		TEST_RUN.setConfigXML("<xml/>");
		TEST_RUN.setBuildNumber(6);
		TEST_RUN.setStatus(Status.FAILED);
		TEST_RUN.setStartedBy(Initiator.SCHEDULER);
		TEST_RUN.setWorkItemId(2L);
		
		testRunMapper.updateTestRun(TEST_RUN);

		checkTestRun(testRunMapper.getTestRunById(TEST_RUN.getId()));
	}

	/**
	 * Turn this in to delete testRun after all tests
	 */
	private static final boolean DELETE_ENABLED = true;

	/**
	 * If true, then <code>deleteTestRun</code> will be used to delete testRun after all tests, otherwise -
	 * <code>deleteTestRunById</code>
	 */
	private static final boolean DELETE_BY_TEST_RUN = false;

	@Test(enabled = ENABLED && DELETE_ENABLED && DELETE_BY_TEST_RUN, dependsOnMethods =
	{ "createTestRun", "getTestRunById", "updateTestRun" })
	public void deleteTestRun()
	{
		testRunMapper.deleteTestRun(TEST_RUN);

		assertNull(testRunMapper.getTestRunById(TEST_RUN.getId()));
	}

	@Test(enabled = ENABLED && DELETE_ENABLED && !DELETE_BY_TEST_RUN, dependsOnMethods =
	{ "createTestRun", "getTestRunById", "updateTestRun" })
	public void deleteTestRunById()
	{
		testRunMapper.deleteTestRunById((TEST_RUN.getId()));

		assertNull(testRunMapper.getTestRunById(TEST_RUN.getId()));
	}

	private void checkTestRun(TestRun testRun)
	{
		assertEquals(testRun.getUser().getId(), TEST_RUN.getUser().getId(), "User ID must match");
		assertEquals(testRun.getTestSuiteId(), TEST_RUN.getTestSuiteId(), "Test suite ID must match");
		assertEquals(testRun.getStatus(), TEST_RUN.getStatus(), "Status must match");
		assertEquals(testRun.getScmURL(), TEST_RUN.getScmURL(), "SCM URL must match");
		assertEquals(testRun.getScmBranch(), TEST_RUN.getScmBranch(), "SCM branch must match");
		assertEquals(testRun.getScmRevision(), TEST_RUN.getScmRevision(), "SCM revision must match");
		assertEquals(testRun.getConfigXML(), TEST_RUN.getConfigXML(), "Config XML must match");
		assertEquals(testRun.getStartedBy(), TEST_RUN.getStartedBy(), "Initiator must match");
		assertEquals(testRun.getBuildNumber(), TEST_RUN.getBuildNumber(), "Build number must match");
		assertEquals(testRun.getWorkItemId(), TEST_RUN.getWorkItemId(), "Work item ID must match");
		assertEquals(testRun.getJob().getId(), TEST_RUN.getJob().getId(), "Job ID must match");
		assertEquals(testRun.getUpstreamJob().getId(), TEST_RUN.getUpstreamJob().getId(), "Upstream job ID must match");
		assertEquals(testRun.getUpstreamJobBuildNumber(), TEST_RUN.getUpstreamJobBuildNumber(), "Upstream job build number must match");
	}
}
