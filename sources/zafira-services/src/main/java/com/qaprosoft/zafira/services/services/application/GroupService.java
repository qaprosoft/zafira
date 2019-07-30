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
package com.qaprosoft.zafira.services.services.application;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qaprosoft.zafira.dbaccess.dao.mysql.application.GroupMapper;
import com.qaprosoft.zafira.models.db.Group;
import com.qaprosoft.zafira.models.db.Group.Role;
import com.qaprosoft.zafira.models.db.Permission;
import com.qaprosoft.zafira.services.exceptions.ServiceException;

@Service
public class GroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

    private final GroupMapper groupMapper;

    public GroupService(GroupMapper groupMapper) {
        this.groupMapper = groupMapper;
    }

    @CachePut(value = "groups", key = "T(com.qaprosoft.zafira.dbaccess.utils.TenancyContext).tenantName + ':' + #group.id")
    @Transactional(rollbackFor = Exception.class)
    public Group createGroup(Group group) {
        groupMapper.createGroup(group);
        addPermissionsToGroup(group);
        return group;
    }

    @CachePut(value = "groups", key = "T(com.qaprosoft.zafira.dbaccess.utils.TenancyContext).tenantName + ':' + #group.id")
    @Transactional(rollbackFor = Exception.class)
    public Group addPermissionsToGroup(Group group) {
        Group dbGroup = groupMapper.getGroupById(group.getId());

        Set<Permission> intersection = new HashSet<>(group.getPermissions());
        intersection.retainAll(dbGroup.getPermissions());

        dbGroup.getPermissions().removeAll(intersection);
        group.getPermissions().removeAll(intersection);
        dbGroup.getPermissions().forEach(permission -> {
            try {
                deletePermissionFromGroup(group.getId(), permission.getId());
            } catch (ServiceException e) {
                LOGGER.error(e.getMessage());
            }
        });
        groupMapper.addPermissionsToGroup(group.getId(), group.getPermissions());
        group.getPermissions().addAll(intersection);
        return group;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "groups", key = "T(com.qaprosoft.zafira.dbaccess.utils.TenancyContext).tenantName + ':' + #id")
    public Group getGroupById(long id) {
        return groupMapper.getGroupById(id);
    }

    @Transactional(readOnly = true)
    public Group getGroupByName(String name) {
        return groupMapper.getGroupByName(name);
    }

    @Transactional(readOnly = true)
    public Group getPrimaryGroupByRole(Role role) {
        return groupMapper.getPrimaryGroupByRole(role);
    }

    @Transactional(readOnly = true)
    public List<Group> getAllGroups(Boolean isPublic) {
        List<Group> groupList = groupMapper.getAllGroups(isPublic);
        for (Group group : groupList) {
            Collections.sort(group.getUsers());
        }
        return groupList;
    }

    public static List<Role> getRoles() {
        return Arrays.asList(Role.values());
    }

    @Transactional(readOnly = true)
    public Integer getGroupsCount() {
        return groupMapper.getGroupsCount();
    }

    @CachePut(value = "groups", key = "T(com.qaprosoft.zafira.dbaccess.utils.TenancyContext).tenantName + ':' + #group.id")
    @Transactional(rollbackFor = Exception.class)
    public Group updateGroup(Group group) {
        groupMapper.updateGroup(group);
        addPermissionsToGroup(group);
        return group;
    }

    @CacheEvict(value = "groups", key = "T(com.qaprosoft.zafira.dbaccess.utils.TenancyContext).tenantName + ':' + #id")
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(long id) {
        groupMapper.deleteGroup(id);
    }

    @CacheEvict(value = "groups", key = "T(com.qaprosoft.zafira.dbaccess.utils.TenancyContext).tenantName + ':' + #groupId")
    @Transactional(rollbackFor = Exception.class)
    public void deletePermissionFromGroup(long groupId, long permissionId) {
        groupMapper.deletePermissionFromGroup(groupId, permissionId);
    }
}