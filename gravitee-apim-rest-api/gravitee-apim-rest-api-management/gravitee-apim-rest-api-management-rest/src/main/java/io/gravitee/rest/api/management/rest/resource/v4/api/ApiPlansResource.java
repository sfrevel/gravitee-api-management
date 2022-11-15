/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.management.rest.resource.v4.api;

import static io.gravitee.rest.api.model.permissions.RolePermission.API_GATEWAY_DEFINITION;
import static io.gravitee.rest.api.model.permissions.RolePermission.API_PLAN;
import static io.gravitee.rest.api.model.permissions.RolePermissionAction.*;
import static java.util.Comparator.comparingInt;

import io.gravitee.common.http.MediaType;
import io.gravitee.rest.api.management.rest.resource.AbstractResource;
import io.gravitee.rest.api.management.rest.resource.v4.param.PlanSecurityParam;
import io.gravitee.rest.api.management.rest.resource.v4.param.PlanStatusParam;
import io.gravitee.rest.api.management.rest.security.Permission;
import io.gravitee.rest.api.management.rest.security.Permissions;
import io.gravitee.rest.api.model.Visibility;
import io.gravitee.rest.api.model.permissions.RolePermission;
import io.gravitee.rest.api.model.permissions.RolePermissionAction;
import io.gravitee.rest.api.model.v4.api.GenericApiEntity;
import io.gravitee.rest.api.model.v4.plan.NewPlanEntity;
import io.gravitee.rest.api.model.v4.plan.PlanEntity;
import io.gravitee.rest.api.model.v4.plan.PlanSecurityType;
import io.gravitee.rest.api.model.v4.plan.PlanType;
import io.gravitee.rest.api.model.v4.plan.UpdatePlanEntity;
import io.gravitee.rest.api.service.GroupService;
import io.gravitee.rest.api.service.common.ExecutionContext;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.gravitee.rest.api.service.exceptions.ForbiddenAccessException;
import io.gravitee.rest.api.service.v4.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@Tag(name = "🧪 V4 - API Plans")
public class ApiPlansResource extends AbstractResource {

    @Inject
    private PlanService planService;

    @Inject
    private GroupService groupService;

    @Context
    private ResourceContext resourceContext;

    @SuppressWarnings("UnresolvedRestParam")
    @PathParam("api")
    @Parameter(name = "api", hidden = true)
    private String apiId;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 List plans for an API",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>List all the plans accessible to the current user."
    )
    @ApiResponse(
        responseCode = "200",
        description = "List accessible plans for current user",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            array = @ArraySchema(schema = @Schema(implementation = PlanEntity.class), uniqueItems = true)
        )
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public List<PlanEntity> getApiPlans(
        @QueryParam("status") @DefaultValue("PUBLISHED") @Parameter(
            explode = Explode.FALSE,
            schema = @Schema(type = "array")
        ) final PlanStatusParam wishedStatus,
        @QueryParam("security") @Parameter(explode = Explode.FALSE, schema = @Schema(type = "array")) final PlanSecurityParam security
    ) {
        final ExecutionContext executionContext = GraviteeContext.getExecutionContext();
        if (
            !hasPermission(executionContext, RolePermission.API_PLAN, apiId, RolePermissionAction.READ) &&
            !hasPermission(executionContext, RolePermission.API_LOG, apiId, RolePermissionAction.READ)
        ) {
            throw new ForbiddenAccessException();
        }

        GenericApiEntity genericApiEntity = apiSearchService.findGenericById(executionContext, apiId);

        return planService
            .findByApi(executionContext, apiId)
            .stream()
            .filter(
                plan ->
                    wishedStatus.contains(plan.getStatus()) &&
                    (
                        (isAuthenticated() && isAdmin()) ||
                        groupService.isUserAuthorizedToAccessApiData(
                            genericApiEntity,
                            plan.getExcludedGroups(),
                            getAuthenticatedUserOrNull()
                        )
                    )
            )
            .filter(plan -> security == null || security.contains(PlanSecurityType.valueOfLabel(plan.getSecurity().getType())))
            .sorted(comparingInt(PlanEntity::getOrder))
            .map(this::filterSensitiveData)
            .collect(Collectors.toList());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Create a plan",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the MANAGE_PLANS permission to use this service"
    )
    @ApiResponse(
        responseCode = "201",
        description = "Plan successfully created",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlanEntity.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = API_PLAN, acls = CREATE) })
    public Response createApiPlan(@Parameter(name = "plan", required = true) @Valid @NotNull NewPlanEntity newPlanEntity) {
        newPlanEntity.setApiId(apiId);
        newPlanEntity.setType(PlanType.API);

        PlanEntity planEntity = planService.create(GraviteeContext.getExecutionContext(), newPlanEntity);

        return Response.created(this.getLocationHeader(planEntity.getId())).entity(planEntity).build();
    }

    @PUT
    @Path("/{plan}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Update a plan",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the MANAGE_PLANS permission to use this service"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Plan successfully updated",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlanEntity.class))
    )
    @ApiResponse(responseCode = "400", description = "Bad plan format")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = API_PLAN, acls = UPDATE) })
    public Response updateApiPlan(
        @PathParam("plan") String plan,
        @Parameter(name = "plan", required = true) @Valid @NotNull UpdatePlanEntity updatePlanEntity
    ) {
        if (updatePlanEntity.getId() != null && !plan.equals(updatePlanEntity.getId())) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("'plan' parameter does not correspond to the plan to update")
                .build();
        }

        // Force ID
        updatePlanEntity.setId(plan);

        final ExecutionContext executionContext = GraviteeContext.getExecutionContext();
        PlanEntity planEntity = planService.findById(executionContext, plan);
        if (!planEntity.getApiId().equals(apiId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'plan' parameter does not correspond to the current API").build();
        }

        planEntity = planService.update(executionContext, updatePlanEntity);
        return Response.ok(planEntity).build();
    }

    @GET
    @Path("/{plan}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Get a plan",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the READ permission to use this service"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Plan information",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlanEntity.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public Response getApiPlan(@PathParam("plan") String plan) {
        final ExecutionContext executionContext = GraviteeContext.getExecutionContext();
        if (
            Visibility.PUBLIC.equals(apiService.findById(executionContext, apiId).getVisibility()) ||
            hasPermission(GraviteeContext.getExecutionContext(), API_PLAN, apiId, READ)
        ) {
            PlanEntity planEntity = planService.findById(executionContext, plan);
            if (!planEntity.getApiId().equals(apiId)) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("'plan' parameter does not correspond to the current API")
                    .build();
            }

            return Response.ok(planEntity).build();
        }
        throw new ForbiddenAccessException();
    }

    @DELETE
    @Path("/{plan}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Delete a plan",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the MANAGE_PLANS permission to use this service"
    )
    @ApiResponse(responseCode = "204", description = "Plan successfully deleted")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = API_PLAN, acls = DELETE) })
    public Response deleteApiPlan(@PathParam("plan") String plan) {
        final ExecutionContext executionContext = GraviteeContext.getExecutionContext();
        PlanEntity planEntity = planService.findById(executionContext, plan);
        if (!planEntity.getApiId().equals(apiId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'plan' parameter does not correspond to the current API").build();
        }

        planService.delete(executionContext, plan);

        return Response.noContent().build();
    }

    @POST
    @Path("/{plan}/_close")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Close  a plan",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the MANAGE_PLANS permission to use this service"
    )
    @ApiResponse(
        responseCode = "204",
        description = "Plan successfully closed",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlanEntity.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = API_PLAN, acls = UPDATE) })
    public Response closeApiPlan(@PathParam("plan") String plan) {
        final ExecutionContext executionContext = GraviteeContext.getExecutionContext();
        PlanEntity planEntity = planService.findById(executionContext, plan);
        if (!planEntity.getApiId().equals(apiId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'plan' parameter does not correspond to the current API").build();
        }

        PlanEntity closedPlan = planService.close(executionContext, plan);

        return Response.ok(closedPlan).build();
    }

    @POST
    @Path("/{plan}/_publish")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Publicly publish plan",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the MANAGE_PLANS permission to use this service"
    )
    @ApiResponse(
        responseCode = "204",
        description = "Plan successfully published",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlanEntity.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = API_PLAN, acls = UPDATE) })
    public Response publishApiPlan(@PathParam("plan") String plan) {
        final ExecutionContext executionContext = GraviteeContext.getExecutionContext();
        PlanEntity planEntity = planService.findById(executionContext, plan);
        if (!planEntity.getApiId().equals(apiId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'plan' parameter does not correspond to the current API").build();
        }

        return Response.ok(planService.publish(executionContext, plan)).build();
    }

    @POST
    @Deprecated
    @Path("/{plan}/_depreciate")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Deprecated, use '_deprecate' instead. Deprecate a plan",
        deprecated = true,
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the API_PLAN[UPDATE] permission to use this service"
    )
    @ApiResponse(
        responseCode = "204",
        description = "Plan successfully deprecated",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlanEntity.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = API_PLAN, acls = UPDATE) })
    public Response depreciateApiPlan(@PathParam("plan") String plan) {
        return this.deprecateApiPlan(plan);
    }

    @POST
    @Path("/{plan}/_deprecate")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "🧪 Deprecate a plan",
        description = "⚠️ This resource is in alpha version. This implies that it is likely to be modified or even removed in future versions. ⚠️. <br><br>User must have the API_PLAN[UPDATE] permission to use this service"
    )
    @ApiResponse(
        responseCode = "204",
        description = "Plan successfully deprecated",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlanEntity.class))
    )
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = API_PLAN, acls = UPDATE) })
    public Response deprecateApiPlan(@PathParam("plan") String plan) {
        final ExecutionContext executionContext = GraviteeContext.getExecutionContext();
        PlanEntity planEntity = planService.findById(executionContext, plan);
        if (!planEntity.getApiId().equals(apiId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'plan' parameter does not correspond to the current API").build();
        }

        return Response.ok(planService.deprecate(executionContext, plan)).build();
    }

    private PlanEntity filterSensitiveData(PlanEntity entity) {
        if (
            hasPermission(GraviteeContext.getExecutionContext(), API_GATEWAY_DEFINITION, entity.getApiId(), RolePermissionAction.READ) &&
            hasPermission(GraviteeContext.getExecutionContext(), API_PLAN, entity.getApiId(), RolePermissionAction.READ)
        ) {
            // Return complete information if user has permission.
            return entity;
        }

        PlanEntity filtered = new PlanEntity();

        filtered.setId(entity.getId());
        filtered.setCharacteristics(entity.getCharacteristics());
        filtered.setName(entity.getName());
        filtered.setDescription(entity.getDescription());
        filtered.setOrder(entity.getOrder());
        filtered.setSecurity(entity.getSecurity());
        filtered.setType(filtered.getType());
        filtered.setValidation(filtered.getValidation());
        filtered.setCommentRequired(entity.isCommentRequired());
        filtered.setCommentMessage(entity.getCommentMessage());
        filtered.setGeneralConditions(entity.getGeneralConditions());

        return filtered;
    }
}
