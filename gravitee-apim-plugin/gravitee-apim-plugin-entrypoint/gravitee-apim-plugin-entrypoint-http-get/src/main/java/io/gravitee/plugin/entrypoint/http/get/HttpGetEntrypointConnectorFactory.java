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
package io.gravitee.plugin.entrypoint.http.get;

import io.gravitee.gateway.jupiter.api.ConnectorMode;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.async.EntrypointAsyncConnectorFactory;
import io.gravitee.gateway.jupiter.api.exception.PluginConfigurationException;
import io.gravitee.plugin.entrypoint.http.get.configuration.HttpGetEntrypointConnectorConfiguration;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
public class HttpGetEntrypointConnectorFactory extends EntrypointAsyncConnectorFactory {

    public HttpGetEntrypointConnectorFactory() {
        super(HttpGetEntrypointConnectorConfiguration.class);
    }

    @Override
    public Set<ConnectorMode> supportedModes() {
        return HttpGetEntrypointConnector.SUPPORTED_MODES;
    }

    @Override
    public HttpGetEntrypointConnector createConnector(final String configuration) {
        try {
            return new HttpGetEntrypointConnector(getConfiguration(configuration));
        } catch (PluginConfigurationException e) {
            log.error("Can't create connector cause no valid configuration", e);
            return null;
        }
    }
}
