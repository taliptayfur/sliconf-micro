package javaday.istanbul.sliconf.micro.admin.controller;

import com.google.api.client.util.Lists;
import io.swagger.annotations.*;
import javaday.istanbul.sliconf.micro.admin.AdminService;
import javaday.istanbul.sliconf.micro.event.model.Event;
import javaday.istanbul.sliconf.micro.response.ResponseMessage;
import javaday.istanbul.sliconf.micro.util.Constants;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.lang.String.valueOf;

@AllArgsConstructor
@Api(value = "admin", authorizations = {@Authorization(value = "Bearer")})
@Path("/service/admin/events")
@Produces("application/json")
@Component
public class ListEvents implements Route {

    private final AdminService adminService;

    @GET
    @ApiOperation(value = "Lists events for admin", nickname = "AdminListEventsRoute")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(required = true, dataType = "string", name = "token", paramType = "header",
                    example = "Authorization: Bearer <tokenValue>"), //

            @ApiImplicitParam(
                    name = "lifeCycleStates", paramType = "query",
                    defaultValue = "ACTIVE",
                    dataType = "string",
                    allowableValues = "ACTIVE, PASSIVE, HAPPENING, FINISHED, DELETED, FAILED",
                    example = "/events?lifeCycleStates=PASSIVE,ACTIVE --> List active or passive events"
            ),

            @ApiImplicitParam(dataType = "string", name = "pageSize",
                    paramType = "query", defaultValue = "20"),
            @ApiImplicitParam(dataType = "string", name = "pageNumber",
                    paramType = "query", defaultValue = "0"),


    })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Success", response = ResponseMessage.class), //
            @ApiResponse(code = 400, message = "Invalid input data", response = ResponseMessage.class), //
            @ApiResponse(code = 401, message = "Unauthorized", response = ResponseMessage.class), //
            @ApiResponse(code = 404, message = "User not found", response = ResponseMessage.class) //
    })

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public ResponseMessage handle(@ApiParam(hidden = true) Request request,
                                  @ApiParam(hidden = true) Response response) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ResponseMessage responseMessage = adminService.checkUserRoleIsAdmin(authentication);

        if(!responseMessage.isStatus())
            return responseMessage;

        QueryParamsMap lifeCycleStates =  request.queryMap("lifeCycleStates");

        List<String> filters = Lists.newArrayList(Arrays.asList(lifeCycleStates.values()[0].split(",")));

        String pageSize = request.queryParamOrDefault("pageSize", "20");
        String pageNumber = request.queryParamOrDefault("pageNumber", "0");
        Pageable pageable;

        try {
            pageable = new PageRequest(Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        } catch (NumberFormatException e) {
            pageable = new PageRequest(0, 20);
        }

        Page<Event> events = adminService.listEvents(filters, pageable);
        String message = "Events listed. Total Events = " + valueOf(events.getTotalElements());
        return new ResponseMessage(true, message, events.getContent());

    }
}