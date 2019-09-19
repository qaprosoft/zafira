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
 *******************************************************************************/
package com.qaprosoft.zafira.services.services.application.integration.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.ExtractHeader;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
import com.qaprosoft.zafira.models.db.Job;
import com.qaprosoft.zafira.models.dto.BuildParameterType;
import com.qaprosoft.zafira.models.dto.JobResult;
import com.qaprosoft.zafira.services.exceptions.ExternalSystemException;
import com.qaprosoft.zafira.services.services.application.SettingsService;
import com.qaprosoft.zafira.services.services.application.integration.AbstractIntegration;
import com.qaprosoft.zafira.services.services.application.integration.context.JenkinsContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.qaprosoft.zafira.models.db.Setting.Tool.JENKINS;
import static com.qaprosoft.zafira.models.dto.BuildParameterType.BuildParameterClass.BOOLEAN;
import static com.qaprosoft.zafira.models.dto.BuildParameterType.BuildParameterClass.HIDDEN;
import static com.qaprosoft.zafira.models.dto.BuildParameterType.BuildParameterClass.STRING;

@Component
public class JenkinsService extends AbstractIntegration<JenkinsContext> {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsService.class);

    private static final String[] REQUIRED_ARGS = new String[]{"scmURL", "branch", "zafiraFields"};

    private static final String ERR_MSG_UNABLE_RUN_JOB = "Unable to build '%s' job";

    private static final String SCANNER_JOB_URL_PATTERN = "%s/job/%s/job/RegisterRepository";
    private static final String SCANNER_JOB_ROOT_URL_PATTERN = "%s/job/RegisterRepository";
    private static final String RESCANNER_JOB_URL_PATTERN = "%s/job/%s/job/%s/job/onPush-%s";
    private static final String RESCANNER_JOB_ROOT_URL_PATTERN = "%s/job/%s/job/onPush-%s";
    private static final String FOLDER_REGEX = ".+job/.+/job.+";

    public JenkinsService(SettingsService settingsService, CryptoService cryptoService) {
        super(settingsService, cryptoService, JENKINS, JenkinsContext.class);
    }

    public boolean rerunJob(Job ciJob, Integer buildNumber, boolean rerunFailures) {
        JobWithDetails job = getJobWithDetails(ciJob);
        Map<String, String> params = getJobParametersMapByBuild(job, buildNumber);

        params.put("rerun_failures", Boolean.toString(rerunFailures));
        params.replace("debug", "false");

        QueueReference reference = buildJobWithParameters(job, params, true);
        return checkReference(reference);
    }

    public boolean debug(Job ciJob, Integer buildNumber) {
        JobWithDetails job = getJobWithDetails(ciJob);
        Map<String, String> params = getJobParametersMapByBuild(job, buildNumber);

        params.replace("debug", "true");
        params.replace("rerun_failures", "true");
        params.replace("thread_count", "1");

        QueueReference reference = buildJobWithParameters(job, params, true);
        return checkReference(reference);
    }


    private JobWithDetails getJobWithDetails(Job ciJob) {
        return getJobWithDetailsByURLOrName(ciJob).orElseThrow(() -> new ExternalSystemException("Unable to get CI job " + ciJob.getJobURL() + " from Jenkins"));
    }

    private Map<String, String> getJobParametersMapByBuild(JobWithDetails job, Integer buildNumber) {
        Build build = job.getBuildByNumber(buildNumber);
        if (build == null) {
            throw new ExternalSystemException("Build " + buildNumber + " is not found");
        }
        BuildWithDetails buildWithDetails;
        try {
            buildWithDetails = build.details();
        } catch (IOException e) {
            throw new ExternalSystemException("Unable to get job " + job.getDisplayName() + ", build " + buildNumber + " parameters from Jenkins", e);
        }
        return buildWithDetails.getParameters();
    }

    private QueueReference buildJobWithParameters(JobWithDetails job, Map<String, String> params, boolean crumbFlag) {
        try {
            return job.build(params, crumbFlag);
        } catch (IOException e) {
            throw new ExternalSystemException("Unable build job " + job.getDisplayName() + " with parameters "+ params + " from Jenkins");
        }
    }

    public JobResult buildJob(Job job, Map<String, String> jobParameters) {
        JobWithDetails ciJob = getJobWithDetails(job);
        return buildJob(ciJob, jobParameters);
    }

    public JobResult buildScannerJob(String repositoryName, Map<String, String> jobParameters, boolean rescan) {
        String jobUrl = buildJobUrl(repositoryName, rescan);
        LOGGER.error("Jenkins job url: " + jobUrl);
        LOGGER.error("Job parameters: " + jobParameters);
        return buildJob(jobUrl, jobParameters);
    }

    public JobResult abortScannerJob(String repositoryName, Integer buildNumber, boolean rescan) {
        String jobUrl = buildJobUrl(repositoryName, rescan);
        return abortJob(jobUrl, buildNumber);
    }

    private String buildJobUrl(String repositoryName, boolean rescan) {
        String jenkinsFolder = context().getFolder();
        String jenkinsHost = context().getJenkinsHost();
        String jobUrl;
        if (rescan) {
            jobUrl = formatReScannerJobUrl(jenkinsFolder, jenkinsHost, repositoryName);
        } else {
            jobUrl = formatScannerJobUrl(jenkinsFolder, jenkinsHost);
        }
        return jobUrl;
    }

    private String formatReScannerJobUrl(String jenkinsFolder, String jenkinsHost, String repositoryName) {
        String reScannerJobUrl;
        if (StringUtils.isEmpty(jenkinsFolder)) {
            reScannerJobUrl = String.format(RESCANNER_JOB_ROOT_URL_PATTERN, jenkinsHost, repositoryName, repositoryName);
        } else {
            reScannerJobUrl = String.format(RESCANNER_JOB_URL_PATTERN, jenkinsHost, jenkinsFolder, repositoryName, repositoryName);
        }
        return reScannerJobUrl;
    }

    private String formatScannerJobUrl(String jenkinsFolder, String jenkinsHost) {
        String scannerJobUrl;
        if (StringUtils.isEmpty(jenkinsFolder)) {
            scannerJobUrl = String.format(SCANNER_JOB_ROOT_URL_PATTERN, jenkinsHost);
        } else {
            scannerJobUrl = String.format(SCANNER_JOB_URL_PATTERN, jenkinsHost, jenkinsFolder);
        }
        return scannerJobUrl;
    }

    private JobResult buildJob(String jobUrl, Map<String, String> jobParameters) {
        JobWithDetails job = getJobByURL(jobUrl)
                .orElseThrow(() -> new ExternalSystemException(String.format(ERR_MSG_UNABLE_RUN_JOB, jobUrl)));
        return buildJob(job, jobParameters);
    }

    private JobResult buildJob(JobWithDetails job, Map<String, String> jobParameters) {
        JobResult result = null;
        try {
            QueueReference reference = job.build(jobParameters, true);
            boolean success = checkReference(reference);
            result = new JobResult(reference.getQueueItemUrlPart(), success);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public JobResult abortJob(String jobUrl, Integer buildNumber) {
        JobWithDetails job = getJobByURL(jobUrl).orElseThrow(() ->
                new ExternalSystemException(String.format("Unable to abort job '%s' with build number %s", jobUrl, buildNumber)));
        return abortJob(job, buildNumber);
    }

    public JobResult abortJob(Job ciJob, Integer buildNumber) {
        JobWithDetails job = getJobWithDetails(ciJob);
        return abortJob(job, buildNumber);
    }


    private JobResult abortJob(JobWithDetails job, Integer buildNumber) {
        JobResult result = null;
        try {
            QueueReference reference = stop(job, buildNumber);
            boolean success = checkReference(reference);
            if (!success) {
                reference = terminate(job, buildNumber);
                success = checkReference(reference);
            }
            result = new JobResult(reference.getQueueItemUrlPart(), success);
        } catch (Exception e) {
            LOGGER.error("Unable to abort Jenkins job:  " + e.getMessage());
        }
        return result;
    }

    private QueueReference stop(JobWithDetails job, Integer buildNumber) throws IOException {
        ExtractHeader location = job.getClient().post(job.getUrl() + buildNumber + "/stop",
                null, ExtractHeader.class);
        return new QueueReference(location.getLocation());
    }

    private QueueReference terminate(JobWithDetails job, Integer buildNumber) throws IOException {
        ExtractHeader location = job.getClient().post(job.getUrl() + buildNumber + "/term",
                null, ExtractHeader.class);
        return new QueueReference(location.getLocation());
    }

    private boolean checkReference(QueueReference reference) {
        return reference != null && !StringUtils.isEmpty(reference.getQueueItemUrlPart());
    }

    public List<BuildParameterType> getBuildParameters(Job ciJob, Integer buildNumber) {
        List<BuildParameterType> jobParameters = null;
        try {
            JobWithDetails job = getJobWithDetails(ciJob);
            jobParameters = getJobParameters(job.getBuildByNumber(buildNumber).details().getActions());
            BuildParameterType buildParameter = new BuildParameterType(HIDDEN, "ci_run_id",
                    UUID.randomUUID().toString());
            jobParameters.add(buildParameter);
        } catch (Exception e) {
            LOGGER.error("Unable to get job:  " + e.getMessage());
        }
        return jobParameters;
    }

    public Optional<Map<String, String>> getBuildParametersMap(Job ciJob, Integer buildNumber) {
        Map<String, String> jobParameters;
        JobWithDetails jobWithDetails = getJobWithDetails(ciJob);
        jobParameters = getJobParametersMapByBuild(jobWithDetails, buildNumber);
        jobParameters.put("ci_run_id", UUID.randomUUID().toString());
        return Optional.of(jobParameters);
    }

    public Map<Integer, String> getBuildConsoleOutputHtml(Job ciJob, Integer buildNumber, Integer stringsCount,
                                                          Integer fullCount) {
        Map<Integer, String> result = new HashMap<>();
        try {
            JobWithDetails jobWithDetails = getJobWithDetails(ciJob);
            BuildWithDetails buildWithDetails = jobWithDetails.getBuildByNumber(buildNumber).details();
            buildWithDetails.isBuilding();
            result = getLastLogStringsByCount(buildWithDetails.getConsoleOutputHtml(), stringsCount, fullCount);
            if (!buildWithDetails.isBuilding()) {
                result.put(-1, buildWithDetails.getDisplayName());
            }
        } catch (IOException e) {
            LOGGER.error("Unable to get console output text: " + e.getMessage());
        }
        return result;
    }

    private Optional<JobWithDetails> getJobWithDetailsByURLOrName(Job ciJob) {
        return ciJob.getJobURL().matches(FOLDER_REGEX) ? getJobByURL(ciJob.getJobURL()) : getJobByName(ciJob.getName());
    }

    private Optional<JobWithDetails> getJobByName(String jobName) {
        return mapContext(context -> {
            JobWithDetails job;
            try {
                job = context.getJenkinsServer().getJob(jobName);
            } catch (IOException e) {
                throw new ExternalSystemException(e.getMessage(), e);
            }
            return job;
        });
    }

    public Integer getBuildNumber(QueueReference queueReference) {
        Integer buildNumber = null;
        int attempts = 5;
        long millisToSleep = 2000;
        JenkinsServer server = context().getJenkinsServer();
        try {
            QueueItem queueItem = null;
            while (attempts > 0) {
                queueItem = server.getQueueItem(queueReference);
                if (queueItem.getExecutable() != null) {
                    break;
                }
                sleep(millisToSleep);
                attempts--;
            }
            Build build = server.getBuild(queueItem);
            buildNumber = build.getNumber();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return buildNumber;
    }

    private Optional<JobWithDetails> getJobByURL(String jobUrl) {
        return mapContext(context -> {
            String folderUrl = jobUrl.substring(0, jobUrl.lastIndexOf("job/"));
            Path path = Paths.get(jobUrl);
            String jobName = path.getName(path.getNameCount() - 1).toString();
            String folderName = path.getName(path.getNameCount() - 3).toString();
            FolderJob folderJob = new FolderJob(folderName, folderUrl);
            return sendGetJobByFolderAndName(folderJob, jobName);
        });
    }

    private JobWithDetails sendGetJobByFolderAndName(FolderJob folderJob, String jobName) {
        try {
            return context().getJenkinsServer().getJob(folderJob, jobName);
        } catch (IOException e) {
            throw new ExternalSystemException(e.getMessage(), e);
        }
    }

    private Map<Integer, String> getLastLogStringsByCount(String log, Integer count, Integer fullCount) {
        Map<Integer, String> logMap = new HashMap<>();
        int zero = 0;
        String[] strings = log.split("\n");
        count = strings.length < count ? strings.length : count;
        if (fullCount != zero) {
            count = strings.length > fullCount ? strings.length - fullCount : zero;
        }
        logMap.put(strings.length,
                String.join("\n", Arrays.copyOfRange(strings, strings.length - count, strings.length)));
        return logMap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<BuildParameterType> getJobParameters(List actions) {
        Collection parameters = Collections2.filter(actions,
                (Predicate<Map<String, Object>>) action -> action.containsKey("parameters"));
        List<BuildParameterType> params = new ArrayList<>();
        if (parameters != null && !parameters.isEmpty()) {
            for (Object o : ((List) ((Map) parameters.toArray()[0]).get("parameters"))) {
                BuildParameterType buildParameter = new BuildParameterType();
                Map<String, Object> param = (Map) o;
                String name = String.valueOf(param.get("name"));
                String value = String.valueOf(param.get("value"));
                String buildParamClass = String.valueOf(param.get("_class"));
                buildParameter.setName(name);
                buildParameter.setValue(value);
                if (buildParamClass.contains("Hide")) {
                    buildParameter.setParameterClass(HIDDEN);
                } else if (buildParamClass.contains("String")) {
                    buildParameter.setParameterClass(STRING);
                } else if (buildParamClass.contains("Boolean")) {
                    buildParameter.setParameterClass(BOOLEAN);
                }
                if (!name.equals("ci_run_id"))
                    params.add(buildParameter);
            }
        }
        return params;
    }

    public Optional<Job> getJobByUrl(String jobUrl) {
        return mapContext(context -> {
            Job job = null;
            JobWithDetails jobWithDetails = getJobByURL(jobUrl).orElse(null);
            if (jobWithDetails != null && jobWithDetails.getUrl() != null) {
                job = new Job(jobWithDetails.getDisplayName(), jobWithDetails.getUrl().replaceAll("/$", ""));
            }
            return job;
        });
    }

    public static boolean checkArguments(Map<String, String> args) {
        return Arrays.stream(REQUIRED_ARGS).noneMatch(arg -> args.get(arg) == null);
    }

    public static String[] getRequiredArgs() {
        return REQUIRED_ARGS;
    }

    @Override
    public boolean isConnected() {
        try {
            return context().getJenkinsServer().isRunning();
        } catch (Exception e) {
            return false;
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
