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
package io.gravitee.gateway.jupiter.core.v4.endpoint;

import io.gravitee.common.service.AbstractService;
import io.gravitee.definition.model.v4.Api;
import io.gravitee.definition.model.v4.endpointgroup.Endpoint;
import io.gravitee.definition.model.v4.endpointgroup.EndpointGroup;
import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnector;
import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnectorFactory;
import io.gravitee.gateway.jupiter.api.context.DeploymentContext;
import io.gravitee.plugin.endpoint.EndpointConnectorPluginManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DefaultEndpointManager extends AbstractService<EndpointManager> implements EndpointManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultEndpointManager.class);

    private final Api api;
    private final EndpointConnectorPluginManager endpointConnectorPluginManager;
    private final DeploymentContext deploymentContext;
    private ManagedEndpointGroup defaultGroup;
    private final Map<String, ManagedEndpointGroup> groupsByName;
    private final Map<String, ManagedEndpoint> endpointsByName;

    public DefaultEndpointManager(
        final Api api,
        final EndpointConnectorPluginManager endpointConnectorPluginManager,
        final DeploymentContext deploymentContext
    ) {
        this.api = api;
        this.endpointsByName = new ConcurrentHashMap<>(1);
        this.groupsByName = new ConcurrentHashMap<>(1);
        this.endpointConnectorPluginManager = endpointConnectorPluginManager;
        this.deploymentContext = deploymentContext;
    }

    @Override
    public ManagedEndpoint next() {
        return next(EndpointCriteria.ENDPOINT_UP);
    }

    @Override
    public ManagedEndpoint next(final EndpointCriteria criteria) {
        final String name = criteria.getName();

        if (name == null) {
            if (defaultGroup != null && criteria.matches(defaultGroup)) {
                return defaultGroup.next();
            }
        } else {
            // First try to find an endpoint by name.
            final ManagedEndpoint managedEndpoint = endpointsByName.get(name);

            if (managedEndpoint != null) {
                if (criteria.matches(managedEndpoint)) {
                    return managedEndpoint;
                }
                return null;
            }

            final ManagedEndpointGroup managedGroup = groupsByName.get(name);

            if (managedGroup != null && criteria.matches(managedGroup)) {
                return managedGroup.next();
            }
        }
        return null;
    }

    @Override
    protected void doStart() throws Exception {
        for (EndpointGroup endpointGroup : api.getEndpointGroups()) {
            final ManagedEndpointGroup managedEndpointGroup = createAndStartGroup(endpointGroup);

            if (defaultGroup == null) {
                defaultGroup = managedEndpointGroup;
            }
        }
    }

    @Override
    public DefaultEndpointManager preStop() {
        for (ManagedEndpoint managedEndpoint : endpointsByName.values()) {
            try {
                managedEndpoint.getConnector().preStop();
            } catch (Exception e) {
                log.warn("An error occurred when pre-stopping endpoint connector [{}].", managedEndpoint.getDefinition().getName());
            }
        }

        return this;
    }

    @Override
    public void doStop() throws Exception {
        for (String endpointName : endpointsByName.keySet()) {
            removeEndpoint(endpointName);
        }

        endpointsByName.clear();
        groupsByName.clear();
    }

    private ManagedEndpointGroup createAndStartGroup(final EndpointGroup endpointGroup) {
        final ManagedEndpointGroup managedEndpointGroup = new ManagedEndpointGroup(endpointGroup);
        groupsByName.put(endpointGroup.getName(), managedEndpointGroup);

        for (Endpoint endpoint : endpointGroup.getEndpoints()) {
            createAndStartEndpoint(managedEndpointGroup, endpoint);
        }

        return managedEndpointGroup;
    }

    private void createAndStartEndpoint(final ManagedEndpointGroup managedEndpointGroup, final Endpoint endpoint) {
        try {
            final String configuration = getEndpointConfiguration(managedEndpointGroup.getDefinition(), endpoint);
            final EndpointConnectorFactory<EndpointConnector> connectorFactory = endpointConnectorPluginManager.getFactoryById(
                endpoint.getType()
            );

            if (connectorFactory == null) {
                log.warn(
                    "Endpoint connector {} cannot be instantiated (no factory of type [{}] found). Skipped.",
                    endpoint.getName(),
                    endpoint.getType()
                );
                return;
            }

            final EndpointConnector connector = connectorFactory.createConnector(deploymentContext, configuration);

            if (connector == null) {
                log.warn("Endpoint connector {} cannot be started. Skipped.", endpoint.getName());
                return;
            }

            connector.start();

            final ManagedEndpoint managedEndpoint = new ManagedEndpoint(endpoint, managedEndpointGroup, connector);
            managedEndpointGroup.addManagedEndpoint(managedEndpoint);
            endpointsByName.put(endpoint.getName(), managedEndpoint);
        } catch (Exception e) {
            log.warn("Unable to properly start the endpoint connector {}: {}. Skipped.", endpoint.getName(), e.getMessage());
        }
    }

    public void removeEndpoint(final String name) {
        try {
            final ManagedEndpoint managedEndpoint = endpointsByName.remove(name);

            if (managedEndpoint != null) {
                managedEndpoint.getGroup().removeManagedEndpoint(managedEndpoint);
                managedEndpoint.getConnector().stop();
            }
        } catch (Exception e) {
            log.warn("Unable to properly stop the endpoint connector {}: {}.", name, e.getMessage());
        }
    }

    private String getEndpointConfiguration(EndpointGroup endpointGroup, Endpoint endpoint) {
        return endpoint.isInheritConfiguration() ? endpointGroup.getSharedConfiguration() : endpoint.getConfiguration();
    }
}
