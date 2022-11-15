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
package io.gravitee.rest.api.service.v4.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.common.component.Lifecycle;
import io.gravitee.definition.model.DefinitionVersion;
import io.gravitee.definition.model.ResponseTemplate;
import io.gravitee.definition.model.v4.ApiType;
import io.gravitee.definition.model.v4.endpointgroup.EndpointGroup;
import io.gravitee.definition.model.v4.flow.Flow;
import io.gravitee.definition.model.v4.flow.FlowMode;
import io.gravitee.definition.model.v4.listener.http.HttpListener;
import io.gravitee.definition.model.v4.property.Property;
import io.gravitee.definition.model.v4.resource.Resource;
import io.gravitee.definition.model.v4.service.ApiServices;
import io.gravitee.repository.management.model.Api;
import io.gravitee.repository.management.model.ApiLifecycleState;
import io.gravitee.repository.management.model.LifecycleState;
import io.gravitee.repository.management.model.Visibility;
import io.gravitee.rest.api.model.ApiMetadataEntity;
import io.gravitee.rest.api.model.CategoryEntity;
import io.gravitee.rest.api.model.PrimaryOwnerEntity;
import io.gravitee.rest.api.model.v4.api.NewApiEntity;
import io.gravitee.rest.api.model.v4.api.UpdateApiEntity;
import io.gravitee.rest.api.model.v4.api.properties.PropertyEntity;
import io.gravitee.rest.api.model.v4.plan.PlanEntity;
import io.gravitee.rest.api.service.CategoryService;
import io.gravitee.rest.api.service.ParameterService;
import io.gravitee.rest.api.service.WorkflowService;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.gravitee.rest.api.service.v4.FlowService;
import io.gravitee.rest.api.service.v4.PlanService;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiMapperTest {

    private ApiMapper apiMapper;

    @Mock
    private PlanService planService;

    @Mock
    private FlowService flowService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private WorkflowService workflowService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        apiMapper =
            new ApiMapper(objectMapper, planService, flowService, parameterService, workflowService, new CategoryMapper(categoryService));
    }

    @Test
    public void shouldCreateEntityFromApiDefinition() throws JsonProcessingException {
        io.gravitee.definition.model.v4.Api apiDefinition = new io.gravitee.definition.model.v4.Api();
        apiDefinition.setDefinitionVersion(DefinitionVersion.V4);
        apiDefinition.setListeners(List.of(new HttpListener()));
        apiDefinition.setEndpointGroups(List.of(new EndpointGroup()));
        apiDefinition.setServices(new ApiServices());
        apiDefinition.setResources(List.of(new Resource()));
        apiDefinition.setProperties(List.of(new Property("key", "value")));
        apiDefinition.setTags(Set.of("tag"));
        apiDefinition.setFlowMode(FlowMode.DEFAULT);
        apiDefinition.setFlows(List.of(new Flow(), new Flow()));
        apiDefinition.setResponseTemplates(Map.of("/", Map.of("/", new ResponseTemplate())));

        Api api = new Api();
        api.setId("id");
        api.setCrossId("crossId");
        api.setType(ApiType.SYNC);
        api.setName("name");
        api.setVersion("version");
        api.setUpdatedAt(new Date());
        api.setDeployedAt(new Date());
        api.setCreatedAt(new Date());
        api.setDescription("description");
        api.setGroups(Set.of("group1"));
        api.setEnvironmentId("environmentId");
        api.setCategories(Set.of("category"));
        api.setPicture("picture");
        api.setBackground("background");
        api.setLabels(List.of("label"));
        api.setLifecycleState(LifecycleState.STARTED);
        api.setVisibility(Visibility.PUBLIC);
        api.setApiLifecycleState(ApiLifecycleState.CREATED);

        api.setDefinition(objectMapper.writeValueAsString(apiDefinition));

        io.gravitee.rest.api.model.v4.api.ApiEntity apiEntity = apiMapper.toEntity(api, new PrimaryOwnerEntity());

        assertThat(apiEntity.getId()).isEqualTo("id");
        assertThat(apiEntity.getCrossId()).isEqualTo("crossId");
        assertThat(apiEntity.getType()).isEqualTo(ApiType.SYNC);
        assertThat(apiEntity.getName()).isEqualTo("name");
        assertThat(apiEntity.getApiVersion()).isEqualTo("version");
        assertThat(apiEntity.getUpdatedAt()).isNotNull();
        assertThat(apiEntity.getDeployedAt()).isNotNull();
        assertThat(apiEntity.getCreatedAt()).isNotNull();
        assertThat(apiEntity.getDescription()).isEqualTo("description");
        assertThat(apiEntity.getGroups().size()).isEqualTo(1);
        assertThat(apiEntity.getReferenceType()).isEqualTo(GraviteeContext.ReferenceContextType.ENVIRONMENT.name());
        assertThat(apiEntity.getReferenceId()).isEqualTo("environmentId");
        assertThat(apiEntity.getCategories().size()).isEqualTo(1);
        assertThat(apiEntity.getPicture()).isEqualTo("picture");
        assertThat(apiEntity.getBackground()).isEqualTo("background");
        assertThat(apiEntity.getLabels().size()).isEqualTo(1);
        assertThat(apiEntity.getLifecycleState()).isEqualTo(io.gravitee.rest.api.model.api.ApiLifecycleState.CREATED);
        assertThat(apiEntity.getState()).isEqualTo(Lifecycle.State.STARTED);
        assertThat(apiEntity.getVisibility()).isEqualTo(io.gravitee.rest.api.model.Visibility.PUBLIC);

        assertThat(apiEntity.getDefinitionVersion()).isEqualTo(DefinitionVersion.V4);
        assertThat(apiEntity.getListeners()).isNotNull();
        assertThat(apiEntity.getListeners().size()).isEqualTo(1);
        assertThat(apiEntity.getEndpointGroups()).isNotNull();
        assertThat(apiEntity.getEndpointGroups().size()).isEqualTo(1);
        assertThat(apiEntity.getServices()).isNotNull();
        assertThat(apiEntity.getResources()).isNotNull();
        assertThat(apiEntity.getResources().size()).isEqualTo(1);
        assertThat(apiEntity.getProperties()).isNotNull();
        assertThat(apiEntity.getProperties().size()).isEqualTo(1);
        assertThat(apiEntity.getTags()).isNotNull();
        assertThat(apiEntity.getTags().size()).isEqualTo(1);
        assertThat(apiEntity.getFlowMode()).isEqualTo(FlowMode.DEFAULT);
        assertThat(apiEntity.getFlows()).isNotNull();
        assertThat(apiEntity.getFlows().size()).isEqualTo(2);
        assertThat(apiEntity.getResponseTemplates()).isNotNull();
        assertThat(apiEntity.getResponseTemplates().size()).isEqualTo(1);
    }

    @Test
    public void shouldCreateEntityIgnoringWrongApiDefinition() throws JsonProcessingException {
        Api api = new Api();
        api.setId("id");
        api.setCrossId("crossId");
        api.setType(ApiType.SYNC);
        api.setName("name");
        api.setVersion("version");
        api.setUpdatedAt(new Date());
        api.setDeployedAt(new Date());
        api.setCreatedAt(new Date());
        api.setDescription("description");
        api.setGroups(Set.of("group1"));
        api.setEnvironmentId("environmentId");
        api.setCategories(Set.of("category"));
        api.setPicture("picture");
        api.setBackground("background");
        api.setLabels(List.of("label"));
        api.setLifecycleState(LifecycleState.STARTED);
        api.setVisibility(Visibility.PUBLIC);
        api.setApiLifecycleState(ApiLifecycleState.CREATED);
        api.setDefinition("wrong api definition");

        io.gravitee.rest.api.model.v4.api.ApiEntity apiEntity = apiMapper.toEntity(api, null);

        assertThat(apiEntity.getId()).isEqualTo("id");
        assertThat(apiEntity.getCrossId()).isEqualTo("crossId");
        assertThat(apiEntity.getType()).isEqualTo(ApiType.SYNC);
        assertThat(apiEntity.getName()).isEqualTo("name");
        assertThat(apiEntity.getApiVersion()).isEqualTo("version");
        assertThat(apiEntity.getUpdatedAt()).isNotNull();
        assertThat(apiEntity.getDeployedAt()).isNotNull();
        assertThat(apiEntity.getCreatedAt()).isNotNull();
        assertThat(apiEntity.getDescription()).isEqualTo("description");
        assertThat(apiEntity.getGroups().size()).isEqualTo(1);
        assertThat(apiEntity.getReferenceType()).isEqualTo(GraviteeContext.ReferenceContextType.ENVIRONMENT.name());
        assertThat(apiEntity.getReferenceId()).isEqualTo("environmentId");
        assertThat(apiEntity.getCategories().size()).isEqualTo(1);
        assertThat(apiEntity.getPicture()).isEqualTo("picture");
        assertThat(apiEntity.getBackground()).isEqualTo("background");
        assertThat(apiEntity.getLabels().size()).isEqualTo(1);
        assertThat(apiEntity.getLifecycleState()).isEqualTo(io.gravitee.rest.api.model.api.ApiLifecycleState.CREATED);
        assertThat(apiEntity.getState()).isEqualTo(Lifecycle.State.STARTED);
        assertThat(apiEntity.getVisibility()).isEqualTo(io.gravitee.rest.api.model.Visibility.PUBLIC);

        assertThat(apiEntity.getDefinitionVersion()).isNull();
        assertThat(apiEntity.getListeners()).isNull();
        assertThat(apiEntity.getEndpointGroups()).isNull();
        assertThat(apiEntity.getServices()).isNull();
        assertThat(apiEntity.getResources()).isEmpty();
        assertThat(apiEntity.getProperties()).isEmpty();
        assertThat(apiEntity.getTags()).isEmpty();
        assertThat(apiEntity.getFlows()).isNull();
        assertThat(apiEntity.getResponseTemplates()).isEmpty();
    }

    @Test
    public void shouldCreateRepositoryApiFromNewEntity() throws JsonProcessingException {
        NewApiEntity newApiEntity = new NewApiEntity();
        newApiEntity.setName("name");
        newApiEntity.setApiVersion("version");
        newApiEntity.setType(ApiType.ASYNC);
        newApiEntity.setDescription("description");
        newApiEntity.setTags(Set.of("tag"));
        newApiEntity.setGroups(Set.of("group1"));
        newApiEntity.setListeners(List.of(new HttpListener()));
        newApiEntity.setEndpointGroups(List.of(new EndpointGroup()));
        newApiEntity.setFlowMode(FlowMode.DEFAULT);
        newApiEntity.setFlows(List.of(new Flow(), new Flow()));

        Api api = apiMapper.toRepository(GraviteeContext.getExecutionContext(), newApiEntity);

        assertThat(api.getId()).isNotNull();
        assertThat(api.getType()).isEqualTo(ApiType.ASYNC);
        assertThat(api.getName()).isEqualTo("name");
        assertThat(api.getVersion()).isEqualTo("version");
        assertThat(api.getCreatedAt()).isNotNull();
        assertThat(api.getUpdatedAt()).isEqualTo(api.getCreatedAt());
        assertThat(api.getDeployedAt()).isNull();
        assertThat(api.getDescription()).isEqualTo("description");
        assertThat(api.getGroups().size()).isEqualTo(1);
        assertThat(api.getEnvironmentId()).isEqualTo("DEFAULT");
        assertThat(api.getApiLifecycleState()).isEqualTo(ApiLifecycleState.CREATED);
        assertThat(api.getLifecycleState()).isEqualTo(LifecycleState.STOPPED);
        assertThat(api.getVisibility()).isEqualTo(Visibility.PRIVATE);
        assertThat(api.getDefinitionVersion()).isEqualTo(DefinitionVersion.V4);

        io.gravitee.definition.model.v4.Api apiDefinition = new io.gravitee.definition.model.v4.Api();
        apiDefinition.setId(api.getId());
        apiDefinition.setName(api.getName());
        apiDefinition.setType(ApiType.ASYNC);
        apiDefinition.setApiVersion(api.getVersion());
        apiDefinition.setDefinitionVersion(DefinitionVersion.V4);
        apiDefinition.setListeners(List.of(new HttpListener()));
        apiDefinition.setEndpointGroups(List.of(new EndpointGroup()));
        apiDefinition.setTags(Set.of("tag"));
        apiDefinition.setFlowMode(FlowMode.DEFAULT);
        apiDefinition.setFlows(List.of(new Flow(), new Flow()));
        assertThat(api.getDefinition()).isEqualTo(objectMapper.writeValueAsString(apiDefinition));
    }

    @Test
    public void shouldCreateRepositoryApiFromUpdateEntity() throws JsonProcessingException {
        UpdateApiEntity updateApiEntity = new UpdateApiEntity();
        updateApiEntity.setId("id");
        updateApiEntity.setCrossId("crossId");
        updateApiEntity.setName("name");
        updateApiEntity.setApiVersion("version");
        updateApiEntity.setDefinitionVersion(DefinitionVersion.V4);
        updateApiEntity.setType(ApiType.ASYNC);
        updateApiEntity.setDescription("description");
        updateApiEntity.setVisibility(io.gravitee.rest.api.model.Visibility.PUBLIC);
        updateApiEntity.setTags(Set.of("tag1", "tag2"));
        updateApiEntity.setPicture("my-picture");
        updateApiEntity.setPictureUrl("/path/to/my/picture");
        updateApiEntity.setBackground("my-background");
        updateApiEntity.setBackgroundUrl("/path/to/my/background");
        updateApiEntity.setCategories(Set.of("existingCatId", "existingCatKey", "unknownCat"));
        updateApiEntity.setLabels(List.of("label1", "label2"));
        updateApiEntity.setGroups(Set.of("group1", "group2"));
        updateApiEntity.setListeners(List.of(new HttpListener()));
        updateApiEntity.setEndpointGroups(List.of(new EndpointGroup()));
        updateApiEntity.setFlowMode(FlowMode.DEFAULT);
        updateApiEntity.setFlows(List.of(new Flow(), new Flow()));
        updateApiEntity.setMetadata(List.of(new ApiMetadataEntity()));
        updateApiEntity.setLifecycleState(io.gravitee.rest.api.model.api.ApiLifecycleState.UNPUBLISHED);
        updateApiEntity.setDisableMembershipNotifications(true);
        updateApiEntity.setProperties(List.of(new PropertyEntity("propKey", "propValue", false, false)));
        updateApiEntity.setResources(List.of(new Resource()));
        updateApiEntity.setPlans(Set.of(new PlanEntity()));

        CategoryEntity existingCategoryByIdEntity = new CategoryEntity();
        existingCategoryByIdEntity.setId("existingCatId");
        CategoryEntity existingCategoryByKeyEntity = new CategoryEntity();
        existingCategoryByKeyEntity.setKey("existingCatKey");
        when(categoryService.findAll(GraviteeContext.getCurrentEnvironment()))
            .thenReturn(List.of(existingCategoryByIdEntity, existingCategoryByKeyEntity));

        Api api = apiMapper.toRepository(GraviteeContext.getExecutionContext(), updateApiEntity);

        assertThat(api.getId()).isEqualTo("id");
        assertThat(api.getEnvironmentId()).isEqualTo("DEFAULT");
        assertThat(api.getCrossId()).isEqualTo("crossId");
        assertThat(api.getName()).isEqualTo("name");
        assertThat(api.getDescription()).isEqualTo("description");
        assertThat(api.getVersion()).isEqualTo("version");
        assertThat(api.getDefinitionVersion()).isEqualTo(DefinitionVersion.V4);
        assertThat(api.getType()).isEqualTo(ApiType.ASYNC);
        assertThat(api.getDeployedAt()).isNull();
        assertThat(api.getCreatedAt()).isNull();
        assertThat(api.getUpdatedAt()).isNotNull();
        assertThat(api.getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(api.getLifecycleState()).isEqualTo(LifecycleState.STOPPED);
        assertThat(api.getPicture()).isEqualTo(updateApiEntity.getPicture());
        assertThat(api.getBackground()).isEqualTo(updateApiEntity.getBackground());
        assertThat(api.getGroups().size()).isEqualTo(2);
        assertThat(api.getCategories().size()).isEqualTo(2);
        assertThat(api.getLabels().size()).isEqualTo(2);
        assertThat(api.isDisableMembershipNotifications()).isTrue();
        assertThat(api.getApiLifecycleState()).isEqualTo(ApiLifecycleState.UNPUBLISHED);

        io.gravitee.definition.model.v4.Api apiDefinition = new io.gravitee.definition.model.v4.Api();
        apiDefinition.setId(api.getId());
        apiDefinition.setName(api.getName());
        apiDefinition.setType(ApiType.ASYNC);
        apiDefinition.setApiVersion(api.getVersion());
        apiDefinition.setDefinitionVersion(DefinitionVersion.V4);
        apiDefinition.setTags(Set.of("tag1", "tag2"));
        apiDefinition.setListeners(List.of(new HttpListener()));
        apiDefinition.setEndpointGroups(List.of(new EndpointGroup()));
        apiDefinition.setProperties(List.of(new Property("propKey", "propValue", false)));
        apiDefinition.setResources(List.of(new Resource()));
        apiDefinition.setFlowMode(FlowMode.DEFAULT);
        apiDefinition.setFlows(List.of(new Flow(), new Flow()));
        apiDefinition.setResponseTemplates(new HashMap<>());
        assertThat(api.getDefinition()).isEqualTo(objectMapper.writeValueAsString(apiDefinition));
    }
}
