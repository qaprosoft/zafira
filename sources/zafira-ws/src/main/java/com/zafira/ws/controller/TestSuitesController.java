package com.zafira.ws.controller;

import javax.validation.Valid;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.zafira.dbaccess.model.TestSuite;
import com.zafira.services.exceptions.ServiceException;
import com.zafira.services.services.TestSuiteService;
import com.zafira.ws.dto.TestSuiteType;

@Controller
@RequestMapping("tests/suites")
public class TestSuitesController extends AbstractController
{
	@Autowired
	private Mapper mapper;
	
	@Autowired
	private TestSuiteService testSuiteService;
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody TestSuiteType createTestSuite(@RequestBody @Valid TestSuiteType testSuite) throws ServiceException
	{
		return mapper.map(testSuiteService.initiateTestSuite(mapper.map(testSuite, TestSuite.class)), TestSuiteType.class);
	}
}
