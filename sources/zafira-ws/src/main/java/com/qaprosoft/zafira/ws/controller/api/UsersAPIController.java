package com.qaprosoft.zafira.ws.controller.api;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.qaprosoft.zafira.models.dto.UserType;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.services.UserService;
import com.qaprosoft.zafira.ws.controller.AbstractController;
import com.qaprosoft.zafira.ws.swagger.annotations.ResponseStatusDetails;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@Api(value = "Users API")
@CrossOrigin
@RequestMapping("api/users")
public class UsersAPIController extends AbstractController
{
	@Autowired
	private UserService userService;
	
	@Autowired
	private Mapper mapper;
	
	@ResponseStatusDetails
	@ApiOperation(value = "Returns user profile", nickname = "getUserProfile", code = 200, httpMethod = "GET", response = UserType.class)
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody UserType getUserProfile() throws ServiceException
	{
		return mapper.map(userService.getUserById(getPrincipalId()), UserType.class);
	}
}
