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
 ******************************************************************************/
package com.qaprosoft.zafira.services.services.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaprosoft.zafira.dbaccess.dao.mysql.application.LauncherMapper;
import com.qaprosoft.zafira.dbaccess.utils.TenancyContext;
import com.qaprosoft.zafira.models.db.Job;
import com.qaprosoft.zafira.models.db.Launcher;
import com.qaprosoft.zafira.models.db.ScmAccount;
import com.qaprosoft.zafira.models.db.User;
import com.qaprosoft.zafira.models.dto.JenkinsLauncherType;
import com.qaprosoft.zafira.models.dto.JobResult;
import com.qaprosoft.zafira.models.dto.ScannedRepoLaunchersType;
import com.qaprosoft.zafira.services.exceptions.IllegalOperationException;
import com.qaprosoft.zafira.services.exceptions.ResourceNotFoundException;
import com.qaprosoft.zafira.services.services.application.integration.context.JenkinsContext;
import com.qaprosoft.zafira.services.services.application.integration.impl.CryptoService;
import com.qaprosoft.zafira.services.services.application.integration.impl.JenkinsService;
import com.qaprosoft.zafira.services.services.application.integration.impl.SeleniumService;
import com.qaprosoft.zafira.services.services.application.scm.GitHubService;
import com.qaprosoft.zafira.services.services.application.scm.ScmAccountService;
import com.qaprosoft.zafira.services.services.auth.JWTService;
import com.qaprosoft.zafira.services.util.URLResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LauncherService {

    private static final Set<String> MANDATORY_ARGUMENTS = Set.of("scmURL", "branch", "zafiraFields");

    private static final String LAUNCHER_JOB_URL_PATTERN = "%s/job/%s/job/launcher";
    private static final String LAUNCHER_JOB_ROOT_URL_PATTERN = "%s/job/launcher";

    private final LauncherMapper launcherMapper;
    private final JenkinsService jenkinsService;
    private final ScmAccountService scmAccountService;
    private final JobsService jobsService;
    private final JWTService jwtService;
    private final GitHubService gitHubService;
    private final SeleniumService seleniumService;
    private final CryptoService cryptoService;
    private final URLResolver urlResolver;

    public LauncherService(
            LauncherMapper launcherMapper,
            JenkinsService jenkinsService,
            ScmAccountService scmAccountService,
            JobsService jobsService,
            JWTService jwtService,
            GitHubService gitHubService,
            SeleniumService seleniumService,
            CryptoService cryptoService,
            URLResolver urlResolver
    ) {
        this.launcherMapper = launcherMapper;
        this.jenkinsService = jenkinsService;
        this.scmAccountService = scmAccountService;
        this.jobsService = jobsService;
        this.jwtService = jwtService;
        this.gitHubService = gitHubService;
        this.seleniumService = seleniumService;
        this.cryptoService = cryptoService;
        this.urlResolver = urlResolver;
    }

    @Transactional(rollbackFor = Exception.class)
    public Launcher createLauncher(Launcher launcher, User owner) {
        if (jenkinsService.isConnected()) {
            JenkinsContext context = jenkinsService.context();
            String jenkinsHost = context.getJenkinsHost();
            String folder = context.getFolder();
            String launcherJobUrl = StringUtils.isEmpty(folder) ?
                    String.format(LAUNCHER_JOB_ROOT_URL_PATTERN, jenkinsHost) :
                    String.format(LAUNCHER_JOB_URL_PATTERN, jenkinsHost, folder);
            Job job = jobsService.getJobByJobURL(launcherJobUrl);
            if (job == null) {
                job = jenkinsService.getJobDetailsFromJenkins(launcherJobUrl);
                job.setJenkinsHost(jenkinsHost);
                job.setUser(owner);
                jobsService.createJob(job);
            }
            launcher.setJob(job);
        }
        launcherMapper.createLauncher(launcher);
        return launcher;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Launcher> createLaunchersForJob(ScannedRepoLaunchersType scannedRepoLaunchersType, User owner) {
        if (!scannedRepoLaunchersType.isSuccess()) {
            return new ArrayList<>();
        }
        ScmAccount scmAccount = scmAccountService.getScmAccountByRepo(scannedRepoLaunchersType.getRepo());
        if (scmAccount == null) {
            throw new ResourceNotFoundException("Unable to find scm account for repo");
        }

        deleteAutoScannedLaunchersByScmAccountId(scmAccount.getId());

        return scannedRepoLaunchersType.getJenkinsLaunchers().stream()
                                       .map(jenkinsLauncherType -> launcherTypeToLauncher(owner, scmAccount, jenkinsLauncherType))
                                       .collect(Collectors.toList());
    }

    private Launcher launcherTypeToLauncher(User owner, ScmAccount scmAccount, JenkinsLauncherType jenkinsLauncherType) {
        String jobUrl = jenkinsLauncherType.getJobUrl();
        Job job = jobsService.getJobByJobURL(jobUrl);
        if (job == null) {
            job = jobsService.createOrUpdateJobByURL(jobUrl, owner);
        }
        Launcher launcher = new Launcher(job.getName(), jenkinsLauncherType.getJobParameters(), scmAccount, job, true);
        launcherMapper.createLauncher(launcher);
        return launcher;
    }

    @Transactional(readOnly = true)
    public Launcher getLauncherById(Long id) {
        return launcherMapper.getLauncherById(id);
    }

    @Transactional(readOnly = true)
    public List<Launcher> getAllLaunchers() {
        return launcherMapper.getAllLaunchers();
    }

    @Transactional(rollbackFor = Exception.class)
    public Launcher updateLauncher(Launcher launcher) {
        launcherMapper.updateLauncher(launcher);
        return launcher;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLauncherById(Long id) {
        launcherMapper.deleteLauncherById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoScannedLaunchersByScmAccountId(Long scmAccountId) {
        launcherMapper.deleteAutoScannedLaunchersByScmAccountId(scmAccountId);
    }

    @Transactional(readOnly = true)
    public String buildLauncherJob(Launcher launcher, User user) throws IOException {

        ScmAccount scmAccount = scmAccountService.getScmAccountById(launcher.getScmAccount().getId());
        if (scmAccount == null) {
            // TODO by nsidorevich on 2019-09-03: review error code, message and exception type
            throw new ResourceNotFoundException("Scm account not found");
        }

        Job job = launcher.getJob();
        if (job == null) {
            // TODO by nsidorevich on 2019-09-03: review error code, message and exception type
            throw new IllegalOperationException("Launcher job not specified");
        }
        
        Map<String, String> jobParameters = new ObjectMapper().readValue(launcher.getModel(), new TypeReference<Map<String, String>>() {});

        String decryptedAccessToken = cryptoService.decrypt(scmAccount.getAccessToken());
        jobParameters.put("scmURL", scmAccount.buildAuthorizedURL(decryptedAccessToken));
        if (!jobParameters.containsKey("branch")) {
            jobParameters.put("branch", "*/master");
        }
        
        // If Selenium integration is enabled pass selenium_host with basic auth as job argument
        if(seleniumService.isConnected()) {
            String seleniumURL = seleniumService.context().getUrl();
            final String username = seleniumService.context().getUser();
            final String password = seleniumService.context().getPassword();
            if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                seleniumURL = String.format("%s//%s:%s@%s", seleniumURL.split("//")[0], username, password, seleniumURL.split("//")[1]);
            }
//            jobParameters.put("selenium_host", seleniumURL);
            jobParameters.put("selenium_url", seleniumURL);
        }

        jobParameters.put("zafira_enabled", "true");
        jobParameters.put("zafira_service_url", urlResolver.buildWebserviceUrl());
        jobParameters.put("zafira_access_token", jwtService.generateAccessToken(user, TenancyContext.getTenantName()));

        String args = jobParameters.entrySet().stream()
                                   .filter(param -> !MANDATORY_ARGUMENTS.contains(param.getKey()))
                                   .map(param -> param.getKey() + "=" + param.getValue())
                                   .collect(Collectors.joining(","));

        jobParameters.put("zafiraFields", args);

        // CiRunId is a random string, needs to define unique correlation between started launcher and real test run starting
        // It must be returned with test run on start in testRun.ciRunId field
        String ciRunId = UUID.randomUUID().toString();
        jobParameters.put("ci_run_id", ciRunId);

        if (!jobParameters.entrySet().containsAll(MANDATORY_ARGUMENTS)) {
            // TODO by nsidorevich on 2019-09-03: review error code, message and exception type
            throw new IllegalOperationException("Required arguments not found");
        }

        jenkinsService.buildJob(job, jobParameters);

        return ciRunId;
    }

    @Transactional(readOnly = true)
    public JobResult buildScannerJob(User user, String branch, long scmAccountId, boolean rescan) {
        ScmAccount scmAccount = scmAccountService.getScmAccountById(scmAccountId);
        if(scmAccount == null) {
            // TODO by nsidorevich on 2019-09-03: review error code, message and exception type
            throw new ResourceNotFoundException("Scm account not found");
        }
        String tenantName = TenancyContext.getTenantName();
        String repositoryName = scmAccount.getRepositoryName();
        String organizationName = scmAccount.getOrganizationName();

        String accessToken = cryptoService.decrypt(scmAccount.getAccessToken());
        String loginName = gitHubService.getLoginName(scmAccount);

        Map<String, String> jobParameters = new HashMap<>();
        jobParameters.put("userId", String.valueOf(user.getId()));
        if (StringUtils.isNotEmpty(jenkinsService.context().getFolder())) {
            jobParameters.put("organization", organizationName);
        }
        jobParameters.put("repo", repositoryName);
        jobParameters.put("branch", branch);
        jobParameters.put("githubUser", loginName);
        jobParameters.put("githubToken", accessToken);
        jobParameters.put("onlyUpdated", String.valueOf(false));
        jobParameters.put("zafira_service_url", urlResolver.buildWebserviceUrl());
        jobParameters.put("zafira_access_token", jwtService.generateAccessToken(user, tenantName));

        String args = jobParameters.entrySet().stream()
                                   .map(param -> param.getKey() + "=" + param.getValue()).collect(Collectors.joining(","));

        jobParameters.put("zafiraFields", args);

        return jenkinsService.buildScannerJob(repositoryName, jobParameters, rescan);
    }

    @Transactional(readOnly = true)
    public void abortScannerJob(long scmAccountId, Integer buildNumber, boolean rescan) {
        ScmAccount scmAccount = scmAccountService.getScmAccountById(scmAccountId);
        if(scmAccount == null) {
            // TODO by nsidorevich on 2019-09-03: review error code, message and exception type
            throw new ResourceNotFoundException("Scm account not found");
        }
        String repositoryName = scmAccount.getRepositoryName();
        jenkinsService.abortScannerJob(repositoryName, buildNumber, rescan);
    }

    public Integer getBuildNumber(String queueItemUrl) {
        return jenkinsService.getBuildNumber(queueItemUrl);
    }

}
