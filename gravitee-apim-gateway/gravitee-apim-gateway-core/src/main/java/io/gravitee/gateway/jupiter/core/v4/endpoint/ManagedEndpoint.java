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

import io.gravitee.definition.model.v4.endpointgroup.Endpoint;
import io.gravitee.definition.model.v4.endpointgroup.EndpointGroup;
import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnector;
import io.gravitee.gateway.jupiter.api.context.DeploymentContext;
import io.gravitee.plugin.endpoint.EndpointConnectorPluginManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage endpoint represents the endpoint definition and its associated instance of connector.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ManagedEndpoint {

    private final Endpoint definition;
    private final ManagedEndpointGroup group;
    private final EndpointConnector connector;
    private Status status;

    public ManagedEndpoint(Endpoint definition, ManagedEndpointGroup group, EndpointConnector connector) {
        this.definition = definition;
        this.group = group;
        this.connector = connector;
        this.status = Status.UP;
    }

    public Endpoint getDefinition() {
        return definition;
    }

    public ManagedEndpointGroup getGroup() {
        return group;
    }

    public EndpointConnector getConnector() {
        return connector;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        UP,
        DOWN,
    }
}
