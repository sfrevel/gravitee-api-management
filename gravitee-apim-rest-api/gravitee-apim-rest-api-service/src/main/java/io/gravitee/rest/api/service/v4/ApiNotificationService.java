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

import io.gravitee.repository.management.model.Api;
import io.gravitee.rest.api.model.v4.api.GenericApiEntity;
import io.gravitee.rest.api.service.common.ExecutionContext;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ApiNotificationService {
    void triggerUpdateNotification(ExecutionContext executionContext, Api api);
    void triggerUpdateNotification(ExecutionContext executionContext, GenericApiEntity api);

    void triggerDeprecatedNotification(ExecutionContext executionContext, GenericApiEntity indexableApi);

    void triggerDeployNotification(ExecutionContext executionContext, GenericApiEntity api);

    void triggerStartNotification(ExecutionContext executionContext, GenericApiEntity indexableApi);

    void triggerStopNotification(ExecutionContext executionContext, GenericApiEntity indexableApi);
}
