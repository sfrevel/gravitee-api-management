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

import static io.gravitee.definition.model.DefinitionContext.*;

import io.gravitee.definition.model.DefinitionContext;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiRepository;
import io.gravitee.repository.management.model.Api;
import io.gravitee.rest.api.model.EventType;
import io.gravitee.rest.api.model.api.ApiDeploymentEntity;
import io.gravitee.rest.api.service.ApiService;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.gravitee.rest.api.service.exceptions.ApiNotManagedException;
import io.gravitee.rest.api.service.exceptions.TechnicalManagementException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiService_DeployTest {

    @Mock
    ApiRepository apiRepository;

    @InjectMocks
    ApiService apiService = new ApiServiceImpl();

    @Test(expected = TechnicalManagementException.class)
    public void shouldThrowIfManagedByKubernetes() throws TechnicalException {
        final String apiId = "kubernetes-api";
        final Api api = new Api();
        api.setId(apiId);
        api.setOrigin(ORIGIN_KUBERNETES);
        Mockito.when(apiRepository.findById(apiId)).thenReturn(Optional.of(api));
        apiService.deploy(GraviteeContext.getExecutionContext(), apiId, "some-user", EventType.STOP_API, new ApiDeploymentEntity());
    }
}
