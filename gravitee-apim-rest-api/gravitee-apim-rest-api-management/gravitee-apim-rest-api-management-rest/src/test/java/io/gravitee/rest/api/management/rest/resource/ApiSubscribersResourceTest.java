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
package io.gravitee.rest.api.management.rest.resource;

import static io.gravitee.common.http.HttpStatusCode.NOT_FOUND_404;
import static io.gravitee.common.http.HttpStatusCode.OK_200;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.gravitee.common.data.domain.Page;
import io.gravitee.rest.api.model.ApplicationEntity;
import io.gravitee.rest.api.model.PrimaryOwnerEntity;
import io.gravitee.rest.api.model.SubscriptionEntity;
import io.gravitee.rest.api.model.UserEntity;
import io.gravitee.rest.api.model.analytics.TopHitsAnalytics;
import io.gravitee.rest.api.model.analytics.query.GroupByQuery;
import io.gravitee.rest.api.model.api.ApiEntity;
import io.gravitee.rest.api.model.application.ApplicationListItem;
import io.gravitee.rest.api.model.common.SortableImpl;
import io.gravitee.rest.api.model.subscription.SubscriptionQuery;
import io.gravitee.rest.api.service.common.GraviteeContext;
import java.io.IOException;
import java.util.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApiSubscribersResourceTest extends AbstractResourceTest {

    private static final String API_ID = "my-api";

    @Override
    protected String contextPath() {
        return "apis/" + API_ID + "";
    }

    @Before
    public void init() throws IOException {
        reset(apiKeyService, subscriptionService, applicationService);
        GraviteeContext.cleanContext();

        ApiEntity mockApi = new ApiEntity();
        mockApi.setId(API_ID);
        UserEntity user = new UserEntity();
        user.setId(USER_NAME);
        PrimaryOwnerEntity primaryOwner = new PrimaryOwnerEntity(user);
        mockApi.setPrimaryOwner(primaryOwner);
        Set<ApiEntity> mockApis = new HashSet<>(Arrays.asList(mockApi));
        doReturn(mockApis)
            .when(apiService)
            .findPublishedByUser(eq(GraviteeContext.getExecutionContext()), any(), argThat(q -> singletonList(API_ID).equals(q.getIds())));
    }

    @After
    public void tearDown() {
        GraviteeContext.cleanContext();
    }

    @Test
    public void shouldGetApiSubscribers() {
        SubscriptionEntity subA1 = new SubscriptionEntity();
        subA1.setApplication("A");
        subA1.setApi(API_ID);
        SubscriptionEntity subB1 = new SubscriptionEntity();
        subB1.setApplication("B");
        subB1.setApi(API_ID);
        SubscriptionEntity subC1 = new SubscriptionEntity();
        subC1.setApplication("C");
        subC1.setApi(API_ID);
        doReturn(Arrays.asList(subB1, subC1, subA1)).when(subscriptionService).search(eq(GraviteeContext.getExecutionContext()), any());

        ApplicationListItem appA = new ApplicationListItem();
        appA.setId("A");
        ApplicationListItem appB = new ApplicationListItem();
        appB.setId("B");
        ApplicationListItem appC = new ApplicationListItem();
        appC.setId("C");
        Page<ApplicationListItem> applications = new Page(Arrays.asList(appA, appB, appC), 1, 10, 42);

        doReturn(applications)
            .when(applicationService)
            .search(
                eq(GraviteeContext.getExecutionContext()),
                argThat(q -> q.getIds().containsAll(Arrays.asList("A", "B", "C"))),
                eq(new SortableImpl("name", true)),
                argThat(pageable -> pageable.getPageNumber() == 1 && pageable.getPageSize() == 20)
            );

        final Response response = envTarget(API_ID).path("subscribers").request().get();
        assertEquals(OK_200, response.getStatus());

        final Collection<ApplicationListItem> applicationsResponse = response.readEntity(new GenericType<>() {});
        assertNotNull(applicationsResponse);
        assertEquals(3, applicationsResponse.size());
    }

    @Test
    public void shouldGetNoSubscribers() {
        doReturn(Collections.emptyList()).when(subscriptionService).search(eq(GraviteeContext.getExecutionContext()), any());

        final Response response = envTarget(API_ID).path("subscribers").request().get();
        assertEquals(OK_200, response.getStatus());

        final Collection<ApplicationListItem> applicationsResponse = response.readEntity(new GenericType<>() {});
        assertNotNull(applicationsResponse);
        assertEquals(0, applicationsResponse.size());
    }
}
