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
package io.gravitee.rest.api.model.v4.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.gravitee.definition.model.v4.flow.Flow;
import io.gravitee.definition.model.v4.plan.PlanSecurity;
import io.gravitee.definition.model.v4.plan.PlanStatus;
import io.gravitee.rest.api.model.DeploymentRequired;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Schema(name = "PlanEntityV4")
public class PlanEntity implements GenericPlanEntity {

    private String id;
    /**
     * The plan crossId uniquely identifies a plan across environments.
     * Plans promoted between environments will share the same crossId.
     */
    private String crossId;
    private String name;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private Date publishedAt;
    private Date closedAt;

    @JsonIgnore
    private Date needRedeployAt;

    /**
     * The way to validate subscriptions
     */
    private PlanValidationType validation;

    private PlanType type;

    @DeploymentRequired
    private PlanSecurity security;

    @DeploymentRequired
    private String selectionRule;

    @DeploymentRequired
    private List<Flow> flows = new ArrayList<>();

    @DeploymentRequired
    private Set<String> tags;

    @DeploymentRequired
    private PlanStatus status;

    @DeploymentRequired
    private String apiId;

    private int order;
    private List<String> characteristics;
    private List<String> excludedGroups;
    private boolean commentRequired;
    private String commentMessage;
    private String generalConditions;

    @Override
    public PlanSecurity getPlanSecurity() {
        return security;
    }

    @Override
    public PlanStatus getPlanStatus() {
        return status;
    }

    @Override
    public PlanValidationType getPlanValidation() {
        return validation;
    }
}
