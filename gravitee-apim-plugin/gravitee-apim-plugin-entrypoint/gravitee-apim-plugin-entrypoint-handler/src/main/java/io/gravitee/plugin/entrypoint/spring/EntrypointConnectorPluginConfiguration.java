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
package io.gravitee.plugin.entrypoint.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.definition.jackson.datatype.GraviteeMapper;
import io.gravitee.gateway.jupiter.api.connector.ConnectorFactoryHelper;
import io.gravitee.plugin.core.api.ConfigurablePluginManager;
import io.gravitee.plugin.entrypoint.EntrypointConnectorClassLoaderFactory;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPlugin;
import io.gravitee.plugin.entrypoint.internal.DefaultEntrypointConnectorConnectorClassLoaderFactory;
import io.gravitee.plugin.entrypoint.internal.DefaultEntrypointConnectorPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class EntrypointConnectorPluginConfiguration {

    @Bean
    public ConfigurablePluginManager<EntrypointConnectorPlugin<?, ?>> entrypointPluginManager(
        final EntrypointConnectorClassLoaderFactory entrypointConnectorClassLoaderFactory,
        final io.gravitee.node.api.configuration.Configuration configuration,
        final ObjectMapper objectMapper
    ) {
        return new DefaultEntrypointConnectorPluginManager(
            entrypointConnectorClassLoaderFactory,
            new ConnectorFactoryHelper(configuration, objectMapper)
        );
    }

    @Bean
    public EntrypointConnectorClassLoaderFactory entrypointClassLoaderFactory() {
        return new DefaultEntrypointConnectorConnectorClassLoaderFactory();
    }
}
