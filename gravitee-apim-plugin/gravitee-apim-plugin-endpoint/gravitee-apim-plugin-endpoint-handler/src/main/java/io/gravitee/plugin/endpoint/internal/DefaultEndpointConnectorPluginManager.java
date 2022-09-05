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
package io.gravitee.plugin.endpoint.internal;

import io.gravitee.gateway.jupiter.api.connector.ConnectorFactory;
import io.gravitee.gateway.jupiter.api.connector.ConnectorFactoryHelper;
import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnector;
import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnectorFactory;
import io.gravitee.gateway.jupiter.api.context.ExecutionContext;
import io.gravitee.plugin.core.api.AbstractConfigurablePluginManager;
import io.gravitee.plugin.core.api.PluginClassLoader;
import io.gravitee.plugin.endpoint.EndpointConnectorClassLoaderFactory;
import io.gravitee.plugin.endpoint.EndpointConnectorPlugin;
import io.gravitee.plugin.endpoint.EndpointConnectorPluginManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author GraviteeSource Team
 */
@SuppressWarnings("unchecked")
public class DefaultEndpointConnectorPluginManager
    extends AbstractConfigurablePluginManager<EndpointConnectorPlugin<?, ?>>
    implements EndpointConnectorPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEndpointConnectorPluginManager.class);
    private final EndpointConnectorClassLoaderFactory classLoaderFactory;
    private final Map<String, ConnectorFactory<? extends EndpointConnector<?>>> factories = new HashMap<>();
    private final ConnectorFactoryHelper connectorFactoryHelper;

    public DefaultEndpointConnectorPluginManager(
        final EndpointConnectorClassLoaderFactory classLoaderFactory,
        final ConnectorFactoryHelper connectorFactoryHelper
    ) {
        this.classLoaderFactory = classLoaderFactory;
        this.connectorFactoryHelper = connectorFactoryHelper;
    }

    @Override
    public void register(final EndpointConnectorPlugin<?, ?> plugin) {
        super.register(plugin);

        // Create endpoint
        PluginClassLoader pluginClassLoader = classLoaderFactory.getOrCreateClassLoader(plugin);
        try {
            final Class<EndpointConnectorFactory<?>> connectorFactoryClass = (Class<EndpointConnectorFactory<?>>) pluginClassLoader.loadClass(
                plugin.clazz()
            );
            EndpointConnectorFactory<?> factory = createFactory(connectorFactoryClass);
            factories.put(plugin.id(), factory);
        } catch (Exception ex) {
            logger.error("Unexpected error while loading endpoint plugin: {}", plugin.clazz(), ex);
        }
    }

    private EndpointConnectorFactory<?> createFactory(final Class<EndpointConnectorFactory<?>> connectorFactoryClass)
        throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        EndpointConnectorFactory<?> factory;
        try {
            Constructor<EndpointConnectorFactory<?>> constructorWithFactoryHelper = connectorFactoryClass.getDeclaredConstructor(
                ConnectorFactoryHelper.class
            );
            factory = constructorWithFactoryHelper.newInstance(connectorFactoryHelper);
        } catch (NoSuchMethodException e) {
            Constructor<EndpointConnectorFactory<?>> emptyConstructor = connectorFactoryClass.getDeclaredConstructor();
            factory = emptyConstructor.newInstance();
        }
        return factory;
    }

    @Override
    public <T extends EndpointConnectorFactory<U>, U extends EndpointConnector<V>, V extends ExecutionContext> T getFactoryById(
        final String endpointPluginId
    ) {
        return (T) factories.get(endpointPluginId);
    }
}
