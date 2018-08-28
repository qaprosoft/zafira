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

import java.util.*;
import java.util.stream.Collectors;

import com.qaprosoft.zafira.models.db.Permission;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qaprosoft.zafira.dbaccess.dao.mysql.application.GroupMapper;
import com.qaprosoft.zafira.models.db.Group;
import com.qaprosoft.zafira.models.db.Group.Role;
import com.qaprosoft.zafira.services.exceptions.ServiceException;

@Service
public class GroupService
{
	private static final Logger LOGGER = Logger.getLogger(GroupService.class);

	@Autowired
	private GroupMapper groupMapper;

	@CachePut(value = "groups", key = "#group.id")
	@Transactional(rollbackFor = Exception.class)
	public Group createGroup(Group group) throws ServiceException
	{
		groupMapper.createGroup(group);
		addPermissionsToGroup(group);
		return group;
	}

	@Transactional(rollbackFor = Exception.class)
	public Group addPermissionsToGroup(Group group) throws ServiceException
	{
		Group dbGroup = groupMapper.getGroupById(group.getId());

		Set<Permission> intersection = new HashSet<>(group.getPermissions());
		intersection.retainAll(dbGroup.getPermissions());

		dbGroup.getPermissions().removeAll(intersection);
		group.getPermissions().removeAll(intersection);
		dbGroup.getPermissions().forEach(permission -> {
			try
			{
				deletePermissionFromGroup(group.getId(), permission.getId());
			} catch (ServiceException e)
			{
				LOGGER.error(e.getMessage());
			}
		});
		groupMapper.addPermissionsToGroup(group.getId(), group.getPermissions());
		group.getPermissions().addAll(intersection);
		return group;
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "groups", key = "#id")
	public Group getGroupById(long id)
	{
		return groupMapper.getGroupById(id);
	}

	@Transactional(readOnly = true)
	public Group getPrimaryGroupByRole(Role role) throws ServiceException
	{
		return groupMapper.getPrimaryGroupByRole(role);
	}

	@Transactional(readOnly = true)
	public List<Group> getAllGroups() throws ServiceException
	{
		List<Group> groupList = groupMapper.getAllGroups();
		for (Group group : groupList)
		{
			Collections.sort(group.getUsers());
		}
		return groupList;
	}

	public static List<Role> getRoles() {
		return Arrays.stream(Role.values()).filter(role -> ! Arrays.asList(Group.getIgnoredRoles()).contains(role)).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Integer getGroupsCount() throws ServiceException
	{
		return groupMapper.getGroupsCount();
	}

	@CachePut(value = "groups", key = "#group.id")
	@Transactional(rollbackFor = Exception.class)
	public Group updateGroup(Group group) throws ServiceException
	{
		groupMapper.updateGroup(group);
		addPermissionsToGroup(group);
		return group;
	}

	@CacheEvict(value = "groups", key = "#id")
	@Transactional(rollbackFor = Exception.class)
	public void deleteGroup(long id) throws ServiceException
	{
		groupMapper.deleteGroup(id);
	}

	@Transactional(rollbackFor = Exception.class)
	public void deletePermissionFromGroup(long groupId, long permissionId) throws ServiceException
	{
		groupMapper.deletePermissionFromGroup(groupId, permissionId);
	}
}