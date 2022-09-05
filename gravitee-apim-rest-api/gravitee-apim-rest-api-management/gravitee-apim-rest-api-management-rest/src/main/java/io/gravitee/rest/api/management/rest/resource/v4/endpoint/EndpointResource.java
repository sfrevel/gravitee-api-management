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
package io.gravitee.rest.api.management.rest.resource.v4.endpoint;

import io.gravitee.common.http.MediaType;
import io.gravitee.rest.api.management.rest.security.Permission;
import io.gravitee.rest.api.management.rest.security.Permissions;
import io.gravitee.rest.api.model.permissions.RolePermission;
import io.gravitee.rest.api.model.permissions.RolePermissionAction;
import io.gravitee.rest.api.model.platform.plugin.PlatformPluginEntity;
import io.gravitee.rest.api.model.v4.connector.ConnectorPluginEntity;
import io.gravitee.rest.api.service.v4.EntrypointConnectorPluginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

/**
 * Defines the REST resources to manage endpoint v4.
 *
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@Tag(name = "Plugins V4")
public class EndpointResource {

    @Context
    private ResourceContext resourceContext;

    @Inject
    private EntrypointConnectorPluginService endpointService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get an endpoint", description = "User must have the ENVIRONMENT_API[READ] permission to use this service")
    @ApiResponse(
        responseCode = "200",
        description = "Entrypoint plugin",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PlatformPluginEntity.class))
    )
    @ApiResponse(responseCode = "404", description = "Entrypoint not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = RolePermission.ENVIRONMENT_API, acls = RolePermissionAction.READ) })
    public ConnectorPluginEntity getEntrypoint(@PathParam("endpoint") String endpoint) {
        return endpointService.findById(endpoint);
    }

    @GET
    @Path("schema")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a endpoint's schema", description = "User must have the ENVIRONMENT_API[READ] permission to use this service")
    @ApiResponse(
        responseCode = "200",
        description = "Entrypoint schema",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = String.class))
    )
    @ApiResponse(responseCode = "404", description = "Entrypoint not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = RolePermission.ENVIRONMENT_API, acls = RolePermissionAction.READ) })
    public String getEntrypointSchema(@PathParam("endpoint") String endpoint) {
        // Check that the endpoint exists
        endpointService.findById(endpoint);

        return endpointService.getSchema(endpoint);
    }

    @GET
    @Path("documentation")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Get a endpoint's documentation",
        description = "User must have the ENVIRONMENT_API[READ] permission to use this service"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Entrypoint documentation",
        content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class))
    )
    @ApiResponse(responseCode = "404", description = "Entrypoint not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @Permissions({ @Permission(value = RolePermission.ENVIRONMENT_API, acls = RolePermissionAction.READ) })
    public String getEntrypointDoc(@PathParam("endpoint") String endpoint) {
        // Check that the endpoint exists
        endpointService.findById(endpoint);

        return endpointService.getDocumentation(endpoint);
    }
}
