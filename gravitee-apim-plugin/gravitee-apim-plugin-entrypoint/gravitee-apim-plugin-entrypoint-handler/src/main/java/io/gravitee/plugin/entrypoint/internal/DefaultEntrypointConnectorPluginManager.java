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
package io.gravitee.plugin.entrypoint.internal;

import io.gravitee.gateway.jupiter.api.connector.ConnectorHelper;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.EntrypointConnectorFactory;
import io.gravitee.plugin.core.api.AbstractConfigurablePluginManager;
import io.gravitee.plugin.core.api.PluginClassLoader;
import io.gravitee.plugin.entrypoint.EntrypointConnectorClassLoaderFactory;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPlugin;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPluginManager;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@SuppressWarnings("unchecked")
public class DefaultEntrypointConnectorPluginManager
    extends AbstractConfigurablePluginManager<EntrypointConnectorPlugin<?, ?>>
    implements EntrypointConnectorPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEntrypointConnectorPluginManager.class);
    private final EntrypointConnectorClassLoaderFactory classLoaderFactory;
    private final ConnectorHelper connectorHelper;
    private final Map<String, EntrypointConnectorFactory<?>> factories = new HashMap<>();

    public DefaultEntrypointConnectorPluginManager(
        final EntrypointConnectorClassLoaderFactory classLoaderFactory,
        final ConnectorHelper connectorHelper
    ) {
        this.classLoaderFactory = classLoaderFactory;
        this.connectorHelper = connectorHelper;
    }

    @Override
    public void register(final EntrypointConnectorPlugin<?, ?> plugin) {
        super.register(plugin);

        // Create entrypoint
        PluginClassLoader pluginClassLoader = classLoaderFactory.getOrCreateClassLoader(plugin);
        try {
            final Class<EntrypointConnectorFactory<?>> connectorFactoryClass = (Class<EntrypointConnectorFactory<?>>) pluginClassLoader.loadClass(
                plugin.clazz()
            );
            EntrypointConnectorFactory<?> factory = createFactory(connectorFactoryClass);
            factories.put(plugin.id(), factory);
        } catch (Exception ex) {
            logger.error("Unexpected error while loading entrypoint plugin: {}", plugin.clazz(), ex);
        }
    }

    private EntrypointConnectorFactory<?> createFactory(final Class<EntrypointConnectorFactory<?>> connectorFactoryClass)
        throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        EntrypointConnectorFactory<?> factory;
        try {
            Constructor<EntrypointConnectorFactory<?>> constructorWithConfigurationHelper = connectorFactoryClass.getDeclaredConstructor(
                ConnectorHelper.class
            );
            factory = constructorWithConfigurationHelper.newInstance(connectorHelper);
        } catch (NoSuchMethodException e) {
            Constructor<EntrypointConnectorFactory<?>> emptyConstructor = connectorFactoryClass.getDeclaredConstructor();
            factory = emptyConstructor.newInstance();
        }
        return factory;
    }

    @Override
    public EntrypointConnectorFactory<?> getFactoryById(final String entrypointPluginId) {
        return factories.get(entrypointPluginId);
    }

    @Override
    public String getSubscriptionSchema(String pluginId) throws IOException {
        return getSchema(pluginId, "subscriptions");
    }
}
