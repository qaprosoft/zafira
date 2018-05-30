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
package com.qaprosoft.zafira.ws.controller;

import com.qaprosoft.zafira.models.dto.aws.FileUploadType;
import com.qaprosoft.zafira.services.exceptions.ServiceException;
import com.qaprosoft.zafira.services.services.jmx.AmazonService;
import com.qaprosoft.zafira.services.services.jmx.google.GoogleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.qaprosoft.zafira.models.dto.aws.FileUploadType.Type;

@Api(value = "Upload files API")
@Controller
@CrossOrigin
@RequestMapping("api/upload")
public class UploadController extends AbstractController
{

	@Autowired
	private AmazonService amazonService;

	@Autowired
	private GoogleService googleService;

	@ApiOperation(value = "Upload file", nickname = "uploadFile", code = 200, httpMethod = "POST", response = String.class)
	@ResponseStatus(HttpStatus.OK)
	@ApiImplicitParams({@ApiImplicitParam(name = "Authorization", paramType = "header")})
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String uploadFile(@RequestHeader(value = "FileType", required = true) Type type, 
			@RequestParam(value = "file", required = true) MultipartFile file) throws ServiceException
	{
		return String.format("{\"url\": \"%s\"}", amazonService.saveFile(new FileUploadType(file, type), getPrincipalId()));
	}

	@ApiOperation(value = "Upload google json file", nickname = "uploadGoogleCredentialsFile", code = 200, httpMethod = "POST", response = String.class)
	@ResponseStatus(HttpStatus.OK)
	@ApiImplicitParams({@ApiImplicitParam(name = "Authorization", paramType = "header")})
	@RequestMapping(value = "google", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	public void uploadGoogleCredentialsFile(@RequestParam(value = "file", required = true) MultipartFile file) throws ServiceException, IOException
	{
		googleService.createCredentialsFile(file.getInputStream(), file.getOriginalFilename());
	}
}
