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
package io.gravitee.rest.api.service.v4;

import io.gravitee.rest.api.model.v4.api.ApiEntity;
import io.gravitee.rest.api.model.v4.api.NewApiEntity;
import io.gravitee.rest.api.model.v4.api.UpdateApiEntity;
import io.gravitee.rest.api.service.common.ExecutionContext;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ApiService {
    ApiEntity create(final ExecutionContext executionContext, final NewApiEntity api, final String userId);

    ApiEntity update(final ExecutionContext executionContext, final String apiId, final UpdateApiEntity api, final String userId);

    ApiEntity update(
        final ExecutionContext executionContext,
        final String apiId,
        final UpdateApiEntity api,
        final boolean checkPlans,
        final String userId
    );

    void delete(final ExecutionContext executionContext, final String apiId, boolean closePlans);
}
