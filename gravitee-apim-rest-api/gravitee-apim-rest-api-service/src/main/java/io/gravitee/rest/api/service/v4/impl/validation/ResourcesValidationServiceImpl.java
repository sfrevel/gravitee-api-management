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
package io.gravitee.rest.api.service.v4.impl.validation;

import io.gravitee.definition.model.v4.resource.Resource;
import io.gravitee.rest.api.service.ResourceService;
import io.gravitee.rest.api.service.impl.TransactionalService;
import io.gravitee.rest.api.service.v4.validation.ResourcesValidationService;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ResourcesValidationServiceImpl extends TransactionalService implements ResourcesValidationService {

    private final ResourceService resourceService;

    public ResourcesValidationServiceImpl(final ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public List<Resource> validateAndSanitize(List<Resource> resources) {
        if (resources != null) {
            resources.stream().filter(Resource::isEnabled).forEach(resourceService::validateResourceConfiguration);
        }
        return resources;
    }
}
