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
package com.qaprosoft.zafira.services.services.application.jobs;

import com.qaprosoft.zafira.models.db.Monitor;
import com.qaprosoft.zafira.models.db.MonitorStatus;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.services.application.EmailService;
import com.qaprosoft.zafira.services.services.application.MonitorService;
import com.qaprosoft.zafira.services.services.application.emails.MonitorEmailMessageNotification;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class MonitorEmailNotificationTask implements Job
{
	private final static Logger LOGGER = LoggerFactory.getLogger(MonitorEmailNotificationTask.class);

	private final static String EMAIL_SUBJECT = "Monitor Alert";
	private final static String EMAIL_TEXT = "";

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
	{
		try
		{
			// Initialize email service for quartz
			EmailService emailService = ((ApplicationContext) jobExecutionContext
					.getScheduler().getContext().get("applicationContext")).getBean(EmailService.class);

			MonitorService monitorService = ((ApplicationContext) jobExecutionContext
					.getScheduler().getContext().get("applicationContext")).getBean(MonitorService.class);

			SchedulerContext schedulerContext = jobExecutionContext.getScheduler().getContext();
			if (schedulerContext == null)
			{
				throw new ServiceException("Scheduler context is null");
			}

			Monitor monitor = (Monitor) schedulerContext.get(jobExecutionContext.getJobDetail().getKey().getName());

			int actualResponseStatus = getResponseCode(monitor);
			boolean codeMatch = monitor.getExpectedCode() == actualResponseStatus;

			monitor.setSuccess(codeMatch);
			monitorService.updateMonitor(monitor, false, false);

			Calendar dateOfPermission = Calendar.getInstance();
			Calendar lastMonitorStatusDate = Calendar.getInstance();
			MonitorStatus lastMonitorStatus = monitorService.getLastMonitorStatus(monitor.getId());
			if(lastMonitorStatus != null)
			{
				lastMonitorStatusDate.setTime(lastMonitorStatus.getCreatedAt());
				dateOfPermission.add(Calendar.HOUR_OF_DAY, -1);
			} else
			{
				dateOfPermission.add(Calendar.MILLISECOND, 1);
			}
			if(dateOfPermission.after(lastMonitorStatusDate))
			{
				monitorService.createMonitorStatus(new MonitorStatus(codeMatch), monitor.getId());
			}

			if (! codeMatch && monitor.isNotificationsEnabled())
			{
				MonitorEmailMessageNotification monitorEmailMessageNotification
						= new MonitorEmailMessageNotification(EMAIL_SUBJECT, EMAIL_TEXT, monitor, actualResponseStatus);
				try
				{
					emailService.sendEmail(monitorEmailMessageNotification, getRecipientList(monitor.getRecipients()));
				} catch (ServiceException e)
				{
					LOGGER.error("Unable to send email!");
				}
			}
		} catch (SchedulerException e1)
		{
			LOGGER.error("Can't get job context!");
		} catch (ServiceException e)
		{
			LOGGER.error("Scheduler context is null");
		}
	}

	public Integer getResponseCode(Monitor monitor)
	{
		int responseCode = 0;
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		switch (monitor.getHttpMethod())
		{
			case GET:
			{
				try
				{
					HttpGet request = new HttpGet(monitor.getUrl());
					request.addHeader("Accept", "*/*");
					responseCode = httpClient.execute(request).getStatusLine().getStatusCode();
				}
				catch (Exception e)
				{
					LOGGER.error(e.getMessage());
				}
				break;
			}
			case PUT:
			{
				try
				{
					HttpPut request = new HttpPut(monitor.getUrl());
					request.addHeader("Content-Type", "application/json");
					request.addHeader("Accept", "*/*");
					request.setEntity(new StringEntity(monitor.getRequestBody(), "UTF-8"));
					responseCode = httpClient.execute(request).getStatusLine().getStatusCode();
				}
				catch (Exception e)
				{
					LOGGER.error(e.getMessage());
				}
				break;
			}
			case POST:
			{
				try
				{
					HttpPost request = new HttpPost(monitor.getUrl());
					request.addHeader("Content-Type", "application/json");
					request.addHeader("Accept", "*/*");
					request.setEntity(new StringEntity(monitor.getRequestBody(), "UTF-8"));
					responseCode = httpClient.execute(request).getStatusLine().getStatusCode();
				}
				catch (Exception e)
				{
					LOGGER.error(e.getMessage());
				}
				break;
			}
			default:
				break;
			}
		return responseCode;
	}

	private String[] getRecipientList(String recipients)
	{
		return recipients.replaceAll(" ", ",").replaceAll(";", ",").split(",");
	}

}
