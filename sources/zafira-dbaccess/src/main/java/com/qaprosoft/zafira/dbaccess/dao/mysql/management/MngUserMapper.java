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
package com.qaprosoft.zafira.dbaccess.dao.mysql.management;

import com.qaprosoft.zafira.models.db.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MngUserMapper
{
	void createUser(User user);

	User getUserById(long id);

	User getUserByUserName(String username);

	List<User> getAllUsers();

	void updateUser(User user);
	
	void updateLastLoginDate(long userId);

	void deleteUserById(long id);

	void addUserToGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);

	void deleteUserFromGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
