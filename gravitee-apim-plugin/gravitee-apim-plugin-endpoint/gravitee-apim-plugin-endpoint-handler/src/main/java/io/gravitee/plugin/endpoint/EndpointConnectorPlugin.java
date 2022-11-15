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
package io.gravitee.plugin.endpoint;

import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnectorConfiguration;
import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnectorFactory;
import io.gravitee.plugin.core.api.ConfigurablePlugin;

/**
 * @author GraviteeSource Team
 */
public interface EndpointConnectorPlugin<T extends EndpointConnectorFactory<?>, C extends EndpointConnectorConfiguration>
    extends ConfigurablePlugin<C> {
    String PLUGIN_TYPE = "endpoint-connector";

    Class<T> connectorFactory();

    @Override
    default String type() {
        return PLUGIN_TYPE;
    }
}
