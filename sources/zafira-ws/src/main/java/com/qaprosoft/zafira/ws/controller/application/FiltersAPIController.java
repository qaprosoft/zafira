package com.qaprosoft.zafira.ws.controller.application;

import com.qaprosoft.zafira.models.db.Filter;
import com.qaprosoft.zafira.models.dto.filter.FilterType;
import com.qaprosoft.zafira.models.dto.filter.Subject;
import com.qaprosoft.zafira.services.exceptions.EntityAlreadyExistsException;
import com.qaprosoft.zafira.services.exceptions.IllegalOperationException;
import com.qaprosoft.zafira.services.services.application.FilterService;
import com.qaprosoft.zafira.ws.controller.AbstractController;
import com.qaprosoft.zafira.ws.swagger.annotations.ResponseStatusDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Api("Filters API")
@CrossOrigin
@RequestMapping(path = "api/filters", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class FiltersAPIController extends AbstractController {

    @Autowired
    private FilterService filterService;

    @Autowired
    private Mapper mapper;

    @ResponseStatusDetails
    @ApiOperation(value = "Create filter", nickname = "createFilter", httpMethod = "POST", response = FilterType.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PostMapping()
    public FilterType createFilter(@RequestBody @Valid FilterType filterType) {
        filterType.setUserId(getPrincipalId());
        filterType.getSubject().sortCriterias();
        Filter filter = mapper.map(filterType, Filter.class);

        if (filterService.isFilterExists(filter)) {
            throw new EntityAlreadyExistsException("name", Filter.class, false);
        }
        if (filter.isPublicAccess() && !isAdmin()) {
            filter.setPublicAccess(false);
        }
        return mapper.map(filterService.createFilter(filter), FilterType.class);
    }

    @ResponseStatusDetails
    @ApiOperation(value = "Get all public filters", nickname = "getAllPublicFilters", httpMethod = "GET", response = List.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @GetMapping("/all/public")
    public List<FilterType> getAllPublicFilters() {
        return filterService.getAllPublicFilters(getPrincipalId()).stream()
                .map(filter -> mapper.map(filter, FilterType.class))
                .collect(Collectors.toList());
    }

    @ResponseStatusDetails
    @ApiOperation(value = "Update filter", nickname = "updateFilter", httpMethod = "PUT", response = FilterType.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @PutMapping()
    public FilterType updateFilter(@RequestBody @Valid FilterType filterType) {
        Filter filter = filterService.getFilterById(filterType.getId());
        if (filter != null && !filter.getUserId().equals(getPrincipalId())) {
            throw new IllegalOperationException("Cannot access to update filter");
        }
        filterType.getSubject().sortCriterias();
        return mapper.map(filterService.updateFilter(mapper.map(filterType, Filter.class), isAdmin()), FilterType.class);
    }

    @ResponseStatusDetails
    @ApiOperation(value = "Delete filter", nickname = "deleteFilter", httpMethod = "DELETE")
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @DeleteMapping("/{id}")
    public void deleteFilter(@PathVariable("id") Long id) {
        Filter filter = filterService.getFilterById(id);
        if (filter != null && !filter.getUserId().equals(getPrincipalId())) {
            throw new IllegalOperationException("Cannot access to delete filter");
        }
        filterService.deleteFilterById(id);
    }

    @ResponseStatusDetails
    @ApiOperation(value = "Get filter builder", nickname = "getBuilder", httpMethod = "GET", response = Subject.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "Authorization", paramType = "header") })
    @GetMapping("/{name}/builder")
    public Subject getBuilder(@PathVariable("name") Subject.Name name) {
        return filterService.getStoredSubject(name);
    }

}
