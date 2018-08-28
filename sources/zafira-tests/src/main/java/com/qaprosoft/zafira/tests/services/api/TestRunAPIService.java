package com.qaprosoft.zafira.tests.services.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.qaprosoft.zafira.models.db.Status;
import com.qaprosoft.zafira.models.dto.TestRunType;
import com.qaprosoft.zafira.models.dto.TestType;
import com.qaprosoft.zafira.tests.models.TestRunViewType;
import com.qaprosoft.zafira.tests.services.api.builders.TestRunTypeBuilder;

public class TestRunAPIService extends AbstractAPIService
{

	private TestAPIService testAPIService = new TestAPIService();

	public TestRunAPIService()
	{
	}

	public TestRunAPIService(TestAPIService testAPIService)
	{
		this.testAPIService = testAPIService;
	}

	public TestRunViewType createTestRun(TestRunTypeBuilder testRunTypeBuilder, Supplier<List<TestType>> testTypeSupplier)
	{
		List<TestType> testTypes = testTypeSupplier.get();
		boolean inProgress = false;
		for(TestType testType : testTypes)
		{
			if(testType.getStatus().equals(Status.IN_PROGRESS))
			{
				inProgress = true;
				break;
			}
		}
		return new TestRunViewType(finishTestRun(testRunTypeBuilder, inProgress), testTypes);
	}

	public TestRunViewType createTestRun(TestRunTypeBuilder testRunTypeBuilder, Integer passedCount, Integer failedCount, Integer inProgressCount, Integer skippedCount,
			Integer abortedCount, int failedMessageLength)
	{
		List<TestType> testTypes = new ArrayList<>();
		return createTestRun(testRunTypeBuilder, () -> {
			testTypes.addAll(testAPIService.createTests(testRunTypeBuilder, passedCount, Status.PASSED, 0));
			testTypes.addAll(testAPIService.createTests(testRunTypeBuilder, failedCount, Status.FAILED, failedMessageLength));
			testTypes.addAll(testAPIService.createTests(testRunTypeBuilder, skippedCount, Status.SKIPPED, failedMessageLength));
			testTypes.addAll(testAPIService.createTests(testRunTypeBuilder, abortedCount, Status.ABORTED, 0));
			testTypes.addAll(testAPIService.createTests(testRunTypeBuilder, inProgressCount, Status.IN_PROGRESS, 0));
			return testTypes;
		});
	}

	public List<TestRunViewType> createTestRunsWithBounds(Integer count, Integer boundPassedCount, Integer boundFailedCount, Integer boundInProgressCount,
			Integer boundSkippedCount, Integer boundAbortedCount, int failedMessageLength)
	{
		return createTestRuns(count, random.nextInt(boundPassedCount), random.nextInt(boundFailedCount),
					random.nextInt(boundInProgressCount), random.nextInt(boundSkippedCount), random.nextInt(boundAbortedCount), failedMessageLength);
	}

	public List<TestRunViewType> createTestRuns(Integer count, Integer passedCount, Integer failedCount, Integer inProgressCount,
			Integer skippedCount, Integer abortedCount, int failedMessageLength)
	{
		List<TestRunViewType> result = new ArrayList<>();
		IntStream.range(0, count).forEach(index -> {
			result.add(createTestRun(new TestRunTypeBuilder(), passedCount, failedCount, inProgressCount, skippedCount, abortedCount, failedMessageLength));
			LOGGER.info("Test run id is " + result.get(index).getTestRunType().getId());
		});
		return result;
	}

	private TestRunType finishTestRun(TestRunTypeBuilder testRunTypeBuilder, boolean inProgress)
	{
		TestRunType testRunType = testRunTypeBuilder.getTestRunType();
		if(! inProgress)
		{
			testRunType = ZAFIRA_CLIENT.updateTestRun(testRunType).getObject();
			testRunType = ZAFIRA_CLIENT.finishTestRun(testRunType.getId()).getObject();
		}
		return testRunType;
	}

	public TestAPIService getTestAPIService()
	{
		return testAPIService;
	}
}
