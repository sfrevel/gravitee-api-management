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
package io.gravitee.repository.management.model;

import io.gravitee.definition.model.DefinitionVersion;
import io.gravitee.definition.model.v4.ApiType;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Api {

    /**
     * Indicates that this api comes Gravitee Kubernetes Operator.
     */
    public static final String ORIGIN_KUBERNETES = "kubernetes";

    /**
     * Indicates that this api comes from Gravitee Management Console.
     */
    public static final String ORIGIN_MANAGEMENT = "management";

    /**
     * Mode indicating the api is fully managed by the origin and so, only the origin should be able to manage the api.
     */
    public static final String MODE_FULLY_MANAGED = "fully_managed";

    /**
     * Mode indicating the api is partially managed by the origin and so, only the origin should be able to manage the the api definition part of the api.
     * This includes everything regarding the definition of the apis (plans, flows, metadata, ...)
     */
    public static final String MODE_API_DEFINITION_ONLY = "api_definition_only";
    /**
     * The api ID.
     */
    private String id;
    /**
     * The ID of the environment the api is attached to
     */
    private String environmentId;
    /**
     * The api crossId uniquely identifies an API across environments.
     * Apis promoted between environments will share the same crossId.
     */
    private String crossId;
    /**
     * The api name.
     */
    private String name;
    /**
     * the api description.
     */
    private String description;
    /**
     * The api version.
     */
    private String version;
    /**
     * The origin of the api (management, kubernetes, ...). Default is {@link Api#ORIGIN_MANAGEMENT}.
     */
    private String origin = ORIGIN_MANAGEMENT;
    /**
     * How the api is managed by the origin (fully, api_definition_only, ...).
     * Default is {@link Api#MODE_FULLY_MANAGED}.
     */
    private String mode = MODE_FULLY_MANAGED;
    /**
     * The api definition version.
     */
    private DefinitionVersion definitionVersion;
    /**
     * The api JSON definition
     */
    private String definition;
    /**
     * The api type.
     */
    private ApiType type;
    /**
     * The api deployment date
     */
    private Date deployedAt;
    /**
     * The Api creation date
     */
    private Date createdAt;
    /**
     * The Api last updated date
     */
    private Date updatedAt;
    /**
     * The api visibility
     */
    private Visibility visibility;
    /**
     * The current runtime life cycle state.
     */
    private LifecycleState lifecycleState = LifecycleState.STOPPED;
    /**
     * The api picture
     */
    private String picture;
    /**
     * the api group, may be null
     */
    private Set<String> groups;
    /**
     * The views associated to this API
     */
    private Set<String> categories;
    /**
     */
    private List<String> labels;
    /**
     */
    private boolean disableMembershipNotifications;
    private ApiLifecycleState apiLifecycleState = ApiLifecycleState.CREATED;
    private String background;

    public Api(Api cloned) {
        this.id = cloned.id;
        this.crossId = cloned.crossId;
        this.environmentId = cloned.environmentId;
        this.name = cloned.name;
        this.description = cloned.description;
        this.origin = cloned.origin;
        this.mode = cloned.mode;
        this.version = cloned.version;
        this.type = cloned.type;
        this.definitionVersion = cloned.definitionVersion;
        this.definition = cloned.definition;
        this.deployedAt = cloned.deployedAt;
        this.createdAt = cloned.createdAt;
        this.updatedAt = cloned.updatedAt;
        this.visibility = cloned.visibility;
        this.lifecycleState = cloned.lifecycleState;
        this.picture = cloned.picture;
        this.background = cloned.background;
        this.groups = cloned.groups;
        this.categories = cloned.categories;
        this.labels = cloned.labels;
        this.apiLifecycleState = cloned.apiLifecycleState;
        this.disableMembershipNotifications = cloned.disableMembershipNotifications;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Api api = (Api) o;
        return Objects.equals(id, api.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public enum AuditEvent implements Audit.ApiAuditEvent {
        API_CREATED,
        API_UPDATED,
        API_DELETED,
        API_ROLLBACKED,
        API_LOGGING_ENABLED,
        API_LOGGING_DISABLED,
        API_LOGGING_UPDATED,
    }
}
