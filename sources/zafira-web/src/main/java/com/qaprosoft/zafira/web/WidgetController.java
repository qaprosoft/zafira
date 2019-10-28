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

import com.qaprosoft.zafira.dbaccess.utils.SQLAdapter;
import com.qaprosoft.zafira.models.db.Attribute;
import com.qaprosoft.zafira.models.db.Widget;
import com.qaprosoft.zafira.models.db.WidgetTemplate;
import com.qaprosoft.zafira.models.dto.SQLExecuteType;
import com.qaprosoft.zafira.models.dto.widget.WidgetTemplateType;
import com.qaprosoft.zafira.models.dto.widget.WidgetType;
import com.qaprosoft.zafira.service.WidgetService;
import com.qaprosoft.zafira.service.WidgetTemplateService;
import com.qaprosoft.zafira.service.exception.ProcessingException;
import com.qaprosoft.zafira.service.exception.ResourceNotFoundException;
import com.qaprosoft.zafira.service.util.URLResolver;
import com.qaprosoft.zafira.web.util.swagger.ApiResponseStatuses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qaprosoft.zafira.service.exception.ProcessingException.ProcessingErrorDetail.WIDGET_QUERY_EXECUTION_ERROR;
import static com.qaprosoft.zafira.service.exception.ResourceNotFoundException.ResourceNotFoundErrorDetail.WIDGET_TEMPLATE_NOT_FOUND;

@ApiIgnore
@RequestMapping(path = "api/widgets", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class WidgetController extends AbstractController {

    private static final String ERR_MSG_WIDGET_TEMPLATE_NOT_FOUND = "Widget template with id %s can not be found";

    private final URLResolver urlResolver;
    private final WidgetService widgetService;
    private final WidgetTemplateService widgetTemplateService;
    private final Mapper mapper;

    public WidgetController(URLResolver urlResolver, WidgetService widgetService,
                            WidgetTemplateService widgetTemplateService,
                            Mapper mapper) {
        this.urlResolver = urlResolver;
        this.widgetService = widgetService;
        this.widgetTemplateService = widgetTemplateService;
        this.mapper = mapper;
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Create widget", nickname = "createWidget", httpMethod = "POST", response = Widget.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PreAuthorize("hasPermission('MODIFY_WIDGETS')")
    @PostMapping()
    public WidgetType createWidget(@RequestBody @Valid WidgetType widget) {
        if (widget.getWidgetTemplate() != null) {
            WidgetTemplate widgetTemplate = prepareWidgetTemplate(widget);
            widget.setType(widgetTemplate.getType().name());
        }
        return mapper.map(widgetService.createWidget(mapper.map(widget, Widget.class)), WidgetType.class);
    }

    private WidgetTemplate prepareWidgetTemplate(@Valid @RequestBody WidgetType widget) {
        long templateId = widget.getWidgetTemplate().getId();
        WidgetTemplate widgetTemplate = widgetTemplateService.getWidgetTemplateById(templateId);
        if (widgetTemplate == null) {
            throw new ResourceNotFoundException(WIDGET_TEMPLATE_NOT_FOUND, ERR_MSG_WIDGET_TEMPLATE_NOT_FOUND, templateId);
        }
        widgetTemplateService.clearRedundantParamsValues(widgetTemplate);
        widget.setWidgetTemplate(mapper.map(widgetTemplate, WidgetTemplateType.class));
        return widgetTemplate;
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Get widget", nickname = "getWidget", httpMethod = "GET", response = Widget.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @GetMapping("/{id}")
    public Widget getWidget(@PathVariable("id") long id) {
        return widgetService.getWidgetById(id);
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Delete widget", nickname = "deleteWidget", httpMethod = "DELETE")
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PreAuthorize("hasPermission('MODIFY_WIDGETS')")
    @DeleteMapping("/{id}")
    public void deleteWidget(@PathVariable("id") long id) {
        widgetService.deleteWidgetById(id);
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Update widget", nickname = "updateWidget", httpMethod = "PUT", response = Widget.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PreAuthorize("hasPermission('MODIFY_WIDGETS')")
    @PutMapping()
    public Widget updateWidget(@RequestBody WidgetType widget) {
        if (widget.getWidgetTemplate() != null) {
            prepareWidgetTemplate(widget);
        }
        return widgetService.updateWidget(mapper.map(widget, Widget.class));
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Execute SQL", nickname = "executeSQL", httpMethod = "POST", response = List.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PostMapping("/sql")
    public List<Map<String, Object>> executeSQL(
            @RequestBody @Valid SQLAdapter sql,
            @RequestParam(name = "projects", defaultValue = "", required = false) List<String> projects,
            @RequestParam(name = "currentUserId", required = false) String currentUserId,
            @RequestParam(name = "dashboardName", required = false) String dashboardName,
            @RequestParam(name = "stackTraceRequired", required = false) boolean stackTraceRequired
    ) {
        String query = sql.getSql();
        List<Map<String, Object>> resultList = null;
        try {
            if (query != null) {
                List<Attribute> attributes = sql.getAttributes();
                if (attributes != null) {
                    for (Attribute attribute : attributes) {
                        query = query.replaceAll("#\\{" + attribute.getKey() + "\\}", attribute.getValue());
                    }
                }

                query = query
                        .replaceAll("#\\{project}", formatProjects(projects))
                        .replaceAll("#\\{dashboardName}", !StringUtils.isEmpty(dashboardName) ? dashboardName : "")
                        .replaceAll("#\\{currentUserId}", !StringUtils.isEmpty(currentUserId) ? currentUserId : String.valueOf(getPrincipalId()))
                        .replaceAll("#\\{currentUserName}", String.valueOf(getPrincipalName()))
                        .replaceAll("#\\{zafiraURL}", urlResolver.buildWebURL())
                        .replaceAll("#\\{hashcode}", "0")
                        .replaceAll("#\\{testCaseId}", "0");

                resultList = widgetService.executeSQL(query);
            }
        } catch (Exception e) {
            if (stackTraceRequired) {
                resultList = new ArrayList<>();
                Map<String, Object> exceptionMap = new HashMap<>();
                exceptionMap.put("Check your query", ExceptionUtils.getFullStackTrace(e));
                resultList.add(exceptionMap);
                return resultList;
            } else {
                throw e;
            }
        }
        return resultList;
    }

    private String formatProjects(List<String> projects) {
        return !CollectionUtils.isEmpty(projects) ? String.join(",", projects) : "%";
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Get all widgets", nickname = "getAllWidgets", httpMethod = "GET", response = List.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @GetMapping()
    public List<WidgetType> getAllWidgets() {
        return widgetService.getAllWidgets()
                .stream()
                .map(widget -> {
                    widgetTemplateService.clearRedundantParamsValues(widget.getWidgetTemplate());
                    return mapper.map(widget, WidgetType.class);
                }).collect(Collectors.toList());
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Get all widget templates", nickname = "getAllWidgetTemplates", httpMethod = "GET", response = List.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @GetMapping("/templates")
    public List<WidgetTemplateType> getAllWidgetTemplates() {
        List<WidgetTemplate> widgetTemplates = widgetTemplateService.getWidgetTemplates();
        return widgetTemplates.stream()
                              .map(widgetTemplate -> mapper.map(widgetTemplate, WidgetTemplateType.class))
                              .collect(Collectors.toList());
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Prepare widget template data by id", nickname = "prepareWidgetTemplateById", httpMethod = "GET", response = WidgetTemplateType.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @GetMapping("/templates/{id}/prepare")
    public WidgetTemplateType prepareWidgetTemplate(@PathVariable("id") Long id) {
        return mapper.map(widgetTemplateService.prepareWidgetTemplateById(id), WidgetTemplateType.class);
    }

    @ApiResponseStatuses
    @ApiOperation(value = "Execute SQL template", nickname = "executeSQLTemplate", httpMethod = "POST", response = List.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PostMapping("/templates/sql")
    public List<Map<String, Object>> executeSQLTemplate(
            @RequestBody @Valid SQLExecuteType sqlExecuteType,
            @RequestParam(value = "stackTraceRequired", required = false) boolean stackTraceRequired
    ) {
        Long templateId = sqlExecuteType.getTemplateId();
        WidgetTemplate widgetTemplate = widgetTemplateService.getWidgetTemplateById(templateId);
        if (widgetTemplate == null) {
            throw new ResourceNotFoundException(WIDGET_TEMPLATE_NOT_FOUND, ERR_MSG_WIDGET_TEMPLATE_NOT_FOUND, templateId);
        }
        List<Map<String, Object>> resultList;
        try {
            Map<WidgetService.DefaultParam, Object> additionalParameters = new HashMap<>();
            additionalParameters.put(WidgetService.DefaultParam.CURRENT_USER_NAME, getPrincipalName());
            additionalParameters.put(WidgetService.DefaultParam.CURRENT_USER_ID, getPrincipalId());
            resultList = widgetService.executeSQL(widgetTemplate.getSql(), sqlExecuteType.getParamsConfig(), additionalParameters, true);
        } catch (Exception e) {
            if (stackTraceRequired) {
                resultList = new ArrayList<>();
                resultList.add(Map.of("Check your query", ExceptionUtils.getFullStackTrace(e)));
                return resultList;
            } else {
                // wrap whatever error is thrown
                throw new ProcessingException(WIDGET_QUERY_EXECUTION_ERROR, e.getMessage(), e);
            }
        }
        return resultList;
    }

}
