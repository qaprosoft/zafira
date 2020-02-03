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
package com.qaprosoft.zafira.web;

import com.qaprosoft.zafira.service.integration.tool.impl.SlackService;
import com.qaprosoft.zafira.web.util.swagger.ApiResponseStatuses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api("Slack API")
@CrossOrigin
@RequestMapping(path = "api/slack", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class SlackController extends AbstractController {

    private final SlackService slackService;

    public SlackController(SlackService slackService) {
        this.slackService = slackService;
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Sends a notification after a test run was reviewed", nickname = "sendReviewNotification", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "Authorization", paramType = "header")})
    @GetMapping("/testrun/{id}/review")
    public void sendOnReviewNotification(@PathVariable("id") long testRunId) {
        slackService.sendStatusReviewed(testRunId);
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Sends a notification after a test run was finished", nickname = "sendOnFinishNotification", httpMethod = "GET")
    @ApiImplicitParams({@ApiImplicitParam(name = "Authorization", paramType = "header")})
    @GetMapping("/testrun/{ciRunId}/finish")
    public void sendOnFinishNotification(@PathVariable("ciRunId") String ciRunId,
                                         @RequestParam(name = "channels", required = false) String channels) {
        slackService.sendStatusOnFinish(ciRunId, channels);
    }

}
