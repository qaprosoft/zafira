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
package com.qaprosoft.zafira.ws.util.dozer;

import org.dozer.DozerConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.qaprosoft.zafira.models.db.User;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.services.application.UserService;

public class LongToUserConverter extends DozerConverter<Long, User>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LongToUserConverter.class);
	@Autowired
	private UserService userService;

	public LongToUserConverter()
	{
		super(Long.class, User.class);
	}

	@Override
	public User convertTo(Long source, User destination)
	{
		try
		{
			return (source == null) ? null : userService.getUserById(source);
		} catch (ServiceException e)
		{
			LOGGER.error("Couldn't get user by id", e);
			return null;
		}
	}

	@Override
	public Long convertFrom(User source, Long destination)
	{
		return (source == null) ? null : source.getId();
	}
}
