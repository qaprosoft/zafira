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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qaprosoft.zafira.dbaccess.dao.mysql.application.WorkItemMapper;
import com.qaprosoft.zafira.models.db.WorkItem;
import com.qaprosoft.zafira.models.db.WorkItem.Type;
import com.qaprosoft.zafira.services.exceptions.ServiceException;

@Service
public class WorkItemService
{
	@Autowired
	private WorkItemMapper workItemMapper;
	
	@Transactional(rollbackFor = Exception.class)
	public void createWorkItem(WorkItem workItem) throws ServiceException
	{
		workItemMapper.createWorkItem(workItem);
	}
	
	@Transactional(readOnly = true)
	public WorkItem getWorkItemById(long id) throws ServiceException
	{
		return workItemMapper.getWorkItemById(id);
	}
	
	@Transactional(readOnly = true)
	public WorkItem getWorkItemByJiraIdAndType(String jiraId, Type type) throws ServiceException
	{
		return workItemMapper.getWorkItemByJiraIdAndType(jiraId, type);
	}
	
	@Transactional(readOnly = true)
	public WorkItem getWorkItemByTestCaseIdAndHashCode(long testCaseId, int hashCode) throws ServiceException
	{
		return workItemMapper.getWorkItemByTestCaseIdAndHashCode(testCaseId, hashCode);
	}
	
	@Transactional(readOnly = true)
	public List<WorkItem> getWorkItemsByTestCaseIdAndType(long testCaseId, Type type) throws ServiceException
	{
		return workItemMapper.getWorkItemsByTestCaseIdAndType(testCaseId, type);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public WorkItem updateWorkItem(WorkItem workItem) throws ServiceException
	{
		workItemMapper.updateWorkItem(workItem);
		return workItem;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteWorkItemById(long id) throws ServiceException
	{
		workItemMapper.deleteWorkItemById(id);
	}
	
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteWorkItem(WorkItem workItem) throws ServiceException
	{
		workItemMapper.deleteWorkItem(workItem);
	}
	
	
	@Transactional(rollbackFor = Exception.class)
	public void deleteKnownIssuesByTestId(long testId) throws ServiceException
	{
		workItemMapper.deleteKnownIssuesByTestId(testId);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public WorkItem createOrGetWorkItem(WorkItem newWorkItem) throws ServiceException
	{
		WorkItem workItem = getWorkItemByJiraIdAndType(newWorkItem.getJiraId(), newWorkItem.getType());
		if(workItem == null)
		{
			createWorkItem(newWorkItem);
			return newWorkItem;
		}
		else
		{
			return workItem;
		}
	}
}