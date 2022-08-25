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
package io.gravitee.rest.api.service.v4.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.gravitee.definition.model.v4.listener.http.ListenerHttp;
import io.gravitee.rest.api.model.api.ApiEntrypointEntity;
import io.gravitee.rest.api.model.parameters.Key;
import io.gravitee.rest.api.model.parameters.ParameterReferenceType;
import io.gravitee.rest.api.model.v4.api.ApiEntity;
import io.gravitee.rest.api.model.v4.api.GenericApiEntity;
import io.gravitee.rest.api.service.EntrypointService;
import io.gravitee.rest.api.service.ParameterService;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.gravitee.rest.api.service.v4.ApiEntrypointService;
import java.util.List;
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
public class ApiEntrypointServiceImplTest {

    @Mock
    private ParameterService parameterService;

    @Mock
    private EntrypointService entrypointService;

    private ApiEntrypointService apiEntrypointService;

    @Before
    public void before() {
        apiEntrypointService = new ApiEntrypointServiceImpl(parameterService, entrypointService);
    }

    @Test
    public void shouldReturnDefaultEntrypointWithoutApiTags() {
        ApiEntity apiEntity = new ApiEntity();
        ListenerHttp e1 = new ListenerHttp();
        apiEntity.setListeners(List.of(e1));
        when(parameterService.find(any(), eq(Key.PORTAL_ENTRYPOINT), any(), eq(ParameterReferenceType.ENVIRONMENT)))
            .thenReturn("https://default-entrypoint");
        List<ApiEntrypointEntity> apiEntrypoints = apiEntrypointService.getApiEntrypoints(GraviteeContext.getExecutionContext(), apiEntity);

        assertThat(apiEntrypoints.size()).isEqualTo(1);
        assertThat(apiEntrypoints.get(0).getHost()).isEqualTo("default-entrypoint");
        assertThat(apiEntrypoints.get(0).getTarget()).isNull();
    }
}
