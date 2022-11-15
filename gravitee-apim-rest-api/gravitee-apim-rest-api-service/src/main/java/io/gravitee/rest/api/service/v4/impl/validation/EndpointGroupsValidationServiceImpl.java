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

import io.gravitee.definition.model.v4.endpointgroup.EndpointGroup;
import io.gravitee.definition.model.v4.endpointgroup.service.EndpointGroupServices;
import io.gravitee.definition.model.v4.endpointgroup.service.EndpointServices;
import io.gravitee.definition.model.v4.service.Service;
import io.gravitee.rest.api.service.exceptions.EndpointMissingException;
import io.gravitee.rest.api.service.exceptions.EndpointNameInvalidException;
import io.gravitee.rest.api.service.impl.TransactionalService;
import io.gravitee.rest.api.service.v4.EndpointConnectorPluginService;
import io.gravitee.rest.api.service.v4.exception.EndpointGroupTypeInvalidException;
import io.gravitee.rest.api.service.v4.exception.EndpointGroupTypeMismatchInvalidException;
import io.gravitee.rest.api.service.v4.exception.EndpointTypeInvalidException;
import io.gravitee.rest.api.service.v4.validation.EndpointGroupsValidationService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class EndpointGroupsValidationServiceImpl extends TransactionalService implements EndpointGroupsValidationService {

    private final EndpointConnectorPluginService endpointService;

    public EndpointGroupsValidationServiceImpl(final EndpointConnectorPluginService endpointService) {
        this.endpointService = endpointService;
    }

    @Override
    public List<EndpointGroup> validateAndSanitize(List<EndpointGroup> endpointGroups) {
        if (endpointGroups == null || endpointGroups.isEmpty()) {
            throw new EndpointMissingException();
        }

        endpointGroups.forEach(
            endpointGroup -> {
                validateName(endpointGroup.getName());
                validateEndpointGroupType(endpointGroup.getType());
                validateServices(endpointGroup.getServices());
                validateEndpointsExistence(endpointGroup);
                if (endpointGroup.getEndpoints() != null && !endpointGroups.isEmpty()) {
                    endpointGroup
                        .getEndpoints()
                        .forEach(
                            endpoint -> {
                                validateName(endpoint.getName());
                                validateEndpointType(endpoint.getType());
                                validateServices(endpoint.getServices());
                                endpoint.setConfiguration(
                                    endpointService.validateConnectorConfiguration(endpoint.getType(), endpoint.getConfiguration())
                                );
                            }
                        );
                }
                validateEndpointsMatchType(endpointGroup);
            }
        );

        return endpointGroups;
    }

    private void validateEndpointsExistence(EndpointGroup endpointGroup) {
        //Is service discovery enabled ?
        Service endpointDiscoveryService = endpointGroup.getServices() == null ? null : endpointGroup.getServices().getDiscovery();
        if (
            (endpointDiscoveryService == null || !endpointDiscoveryService.isEnabled()) &&
            (endpointGroup.getEndpoints() == null || endpointGroup.getEndpoints().isEmpty())
        ) {
            throw new EndpointMissingException();
        }
    }

    private void validateDiscovery(Service discovery) {
        // TODO FCY: Nothing is done today to validate discovery validation. Could be handled by the connector (with a JSON Schema for instance).
    }

    private void validateEndpointType(final String type) {
        if (StringUtils.isBlank(type)) {
            throw new EndpointTypeInvalidException(type);
        }
    }

    private void validateEndpointGroupType(final String type) {
        if (StringUtils.isBlank(type)) {
            throw new EndpointGroupTypeInvalidException(type);
        }
    }

    private void validateEndpointsMatchType(final EndpointGroup endpointGroup) {
        if (endpointGroup.getEndpoints() != null && !endpointGroup.getEndpoints().isEmpty()) {
            boolean allMatchGroupType = endpointGroup
                .getEndpoints()
                .stream()
                .allMatch(endpoint -> endpointGroup.getType().equals(endpoint.getType()));
            if (!allMatchGroupType) {
                throw new EndpointGroupTypeMismatchInvalidException(endpointGroup.getType());
            }
        }
    }

    private void validateHealthCheck(Service healthCheck) {
        // TODO FCY: As the health-check validation configuration is just a String in V4 definition, it's not possible to validate it here.
        //  Will have to be implemented in the connector service (with a JSON Schema for instance).
    }

    private void validateName(final String name) {
        if (name != null && name.contains(":")) {
            throw new EndpointNameInvalidException(name);
        }
    }

    private void validateServices(EndpointGroupServices services) {
        if (services != null) {
            if (services.getDiscovery() != null) {
                validateDiscovery(services.getDiscovery());
            }
            if (services.getHealthCheck() != null) {
                validateHealthCheck(services.getHealthCheck());
            }
        }
    }

    private void validateServices(EndpointServices services) {
        if (services != null) {
            if (services.getHealthCheck() != null) {
                validateHealthCheck(services.getHealthCheck());
            }
        }
    }
}
