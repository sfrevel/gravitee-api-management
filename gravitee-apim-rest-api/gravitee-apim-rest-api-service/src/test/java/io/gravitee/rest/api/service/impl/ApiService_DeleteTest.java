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
package io.gravitee.rest.api.service.impl;

import static io.gravitee.rest.api.model.EventType.PUBLISH_API;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.gravitee.definition.jackson.datatype.GraviteeMapper;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiQualityRuleRepository;
import io.gravitee.repository.management.api.ApiRepository;
import io.gravitee.repository.management.model.Api;
import io.gravitee.repository.management.model.ApiLifecycleState;
import io.gravitee.repository.management.model.LifecycleState;
import io.gravitee.repository.management.model.Plan;
import io.gravitee.repository.management.model.flow.FlowReferenceType;
import io.gravitee.rest.api.model.*;
import io.gravitee.rest.api.model.mixin.ApiMixin;
import io.gravitee.rest.api.service.*;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.gravitee.rest.api.service.configuration.flow.FlowService;
import io.gravitee.rest.api.service.converter.ApiConverter;
import io.gravitee.rest.api.service.exceptions.ApiNotDeletableException;
import io.gravitee.rest.api.service.exceptions.ApiRunningStateException;
import io.gravitee.rest.api.service.jackson.filter.ApiPermissionFilter;
import io.gravitee.rest.api.service.notification.ApiHook;
import io.gravitee.rest.api.service.search.SearchEngineService;
import io.gravitee.rest.api.service.v4.mapper.CategoryMapper;
import java.util.Collections;
import java.util.Optional;
import org.checkerframework.checker.units.qual.A;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiService_DeleteTest {

    private static final String API_ID = "id-api";
    private static final String PLAN_ID = "my-plan";

    @InjectMocks
    private ApiServiceImpl apiService = new ApiServiceImpl();

    @Spy
    private ObjectMapper objectMapper = new GraviteeMapper();

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private PlanService planService;

    @Mock
    private FlowService flowService;

    @Mock
    private MembershipService membershipService;

    @Mock
    private EventService eventService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private AuditService auditService;

    @Mock
    private TopApiService topApiService;

    @Mock
    private AlertService alertService;

    @Mock
    private PageService pageService;

    @Mock
    private GenericNotificationConfigService genericNotificationConfigService;

    @Mock
    private PortalNotificationConfigService portalNotificationConfigService;

    @Mock
    private ApiQualityRuleRepository apiQualityRuleRepository;

    @Mock
    private SearchEngineService searchEngineService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private MediaService mediaService;

    @Mock
    private ApiMetadataService apiMetadataService;

    @Spy
    private CategoryMapper categoryMapper = new CategoryMapper(mock(CategoryService.class));

    @InjectMocks
    private ApiConverter apiConverter = Mockito.spy(new ApiConverter());

    private Api api;
    private PlanEntity planEntity;

    @Before
    public void setUp() throws TechnicalException {
        PropertyFilter apiMembershipTypeFilter = new ApiPermissionFilter();
        objectMapper.setFilterProvider(
            new SimpleFilterProvider(Collections.singletonMap("apiMembershipTypeFilter", apiMembershipTypeFilter))
        );
        api = new Api();
        api.setId(API_ID);
        planEntity = new PlanEntity();
        planEntity.setId(PLAN_ID);
        planEntity.setApi(API_ID);

        lenient().when(apiRepository.findById(API_ID)).thenReturn(Optional.of(api));
        lenient().when(planService.findByApi(GraviteeContext.getExecutionContext(), API_ID)).thenReturn(Collections.singleton(planEntity));
    }

    @Test(expected = ApiRunningStateException.class)
    public void shouldNotDeleteBecauseRunningState() throws TechnicalException {
        api.setLifecycleState(LifecycleState.STARTED);

        apiService.delete(GraviteeContext.getExecutionContext(), API_ID);
    }

    @Test
    public void shouldDeleteBecauseNoPlan() throws TechnicalException {
        api.setLifecycleState(LifecycleState.STOPPED);
        when(planService.findByApi(GraviteeContext.getExecutionContext(), API_ID)).thenReturn(Collections.emptySet());

        apiService.delete(GraviteeContext.getExecutionContext(), API_ID);
        verify(membershipService, times(1)).deleteReference(GraviteeContext.getExecutionContext(), MembershipReferenceType.API, API_ID);
        verify(mediaService, times(1)).deleteAllByApi(API_ID);
        verify(apiMetadataService, times(1)).deleteAllByApi(eq(GraviteeContext.getExecutionContext()), eq(API_ID));
        verify(flowService, times(1)).save(FlowReferenceType.API, API_ID, null);
    }

    @Test(expected = ApiNotDeletableException.class)
    public void shouldNotDeleteBecausePlanNotClosed() throws TechnicalException {
        api.setLifecycleState(LifecycleState.STOPPED);
        planEntity.setStatus(PlanStatus.PUBLISHED);

        apiService.delete(GraviteeContext.getExecutionContext(), API_ID);
        verify(membershipService, times(1)).deleteReference(GraviteeContext.getExecutionContext(), MembershipReferenceType.API, API_ID);
    }

    @Test
    public void shouldDeleteBecausePlanClosed() throws TechnicalException {
        api.setLifecycleState(LifecycleState.STOPPED);
        planEntity.setStatus(PlanStatus.CLOSED);

        apiService.delete(GraviteeContext.getExecutionContext(), API_ID);

        verify(planService, times(1)).delete(GraviteeContext.getExecutionContext(), PLAN_ID);
        verify(membershipService, times(1)).deleteReference(GraviteeContext.getExecutionContext(), MembershipReferenceType.API, API_ID);
        verify(flowService, times(1)).save(FlowReferenceType.API, API_ID, null);
    }

    @Test
    public void shouldDeleteBecausePlanStaging() throws TechnicalException {
        api.setLifecycleState(LifecycleState.STOPPED);
        planEntity.setStatus(PlanStatus.STAGING);

        apiService.delete(GraviteeContext.getExecutionContext(), API_ID);

        verify(planService, times(1)).delete(GraviteeContext.getExecutionContext(), PLAN_ID);
        verify(apiQualityRuleRepository, times(1)).deleteByApi(API_ID);
        verify(membershipService, times(1)).deleteReference(GraviteeContext.getExecutionContext(), MembershipReferenceType.API, API_ID);
        verify(mediaService, times(1)).deleteAllByApi(API_ID);
        verify(apiMetadataService, times(1)).deleteAllByApi(eq(GraviteeContext.getExecutionContext()), eq(API_ID));
        verify(flowService, times(1)).save(FlowReferenceType.API, API_ID, null);
    }

    @Test
    public void shouldDeleteWithKubernetesOrigin() throws Exception {
        api.setOrigin(Api.ORIGIN_KUBERNETES);
        planEntity.setStatus(PlanStatus.PUBLISHED);

        apiService.delete(GraviteeContext.getExecutionContext(), API_ID);

        verify(planService, times(1)).close(eq(GraviteeContext.getExecutionContext()), eq(PLAN_ID));
        verify(apiRepository, times(1))
            .update(
                argThat(
                    _api ->
                        _api.getOrigin().equals(Api.ORIGIN_KUBERNETES) &&
                        _api.getLifecycleState().equals(LifecycleState.STOPPED) &&
                        _api.getApiLifecycleState().equals(ApiLifecycleState.UNPUBLISHED) &&
                        _api.getVisibility().equals(io.gravitee.repository.management.model.Visibility.PRIVATE)
                )
            );
        verify(searchEngineService, times(1))
            .delete(eq(GraviteeContext.getExecutionContext()), argThat(_api -> _api.getId().equals(API_ID)));
    }
}
