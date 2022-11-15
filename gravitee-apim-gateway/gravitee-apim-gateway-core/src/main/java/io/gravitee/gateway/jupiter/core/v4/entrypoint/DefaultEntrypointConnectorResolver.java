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
package io.gravitee.gateway.jupiter.core.v4.entrypoint;

import static io.gravitee.gateway.jupiter.api.context.InternalContextAttributes.ATTR_INTERNAL_LISTENER_TYPE;

import io.gravitee.common.service.AbstractService;
import io.gravitee.definition.model.v4.Api;
import io.gravitee.definition.model.v4.listener.entrypoint.Entrypoint;
import io.gravitee.gateway.jupiter.api.ApiType;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.EntrypointConnector;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.EntrypointConnectorFactory;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.async.EntrypointAsyncConnectorFactory;
import io.gravitee.gateway.jupiter.api.context.ExecutionContext;
import io.gravitee.gateway.jupiter.api.qos.Qos;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPluginManager;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@SuppressWarnings("unchecked")
public class DefaultEntrypointConnectorResolver extends AbstractService<DefaultEntrypointConnectorResolver> {

    private static final Logger log = LoggerFactory.getLogger(DefaultEntrypointConnectorResolver.class);
    private final List<EntrypointConnector> entrypointConnectors;

    public DefaultEntrypointConnectorResolver(final Api api, final EntrypointConnectorPluginManager entrypointConnectorPluginManager) {
        entrypointConnectors =
            api
                .getListeners()
                .stream()
                .flatMap(listener -> listener.getEntrypoints().stream())
                .map(entrypoint -> this.<EntrypointConnector>createConnector(entrypointConnectorPluginManager, entrypoint))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(EntrypointConnector::matchCriteriaCount).reversed())
                .collect(Collectors.toList());
    }

    private <T extends EntrypointConnector> T createConnector(
        EntrypointConnectorPluginManager entrypointConnectorPluginManager,
        Entrypoint entrypoint
    ) {
        EntrypointConnectorFactory<?> connectorFactory = entrypointConnectorPluginManager.getFactoryById(entrypoint.getType());

        if (connectorFactory != null) {
            if (connectorFactory.supportedApi() == ApiType.ASYNC) {
                EntrypointAsyncConnectorFactory entrypointAsyncConnectorFactory = (EntrypointAsyncConnectorFactory) connectorFactory;
                Qos qos = Qos.BALANCED;
                if (entrypoint.getQos() != null) {
                    qos = Qos.fromLabel(entrypoint.getQos().getLabel());
                }
                return (T) entrypointAsyncConnectorFactory.createConnector(qos, entrypoint.getConfiguration());
            }
            return (T) connectorFactory.createConnector(entrypoint.getConfiguration());
        }
        return null;
    }

    public <T extends EntrypointConnector> T resolve(final ExecutionContext ctx) {
        Optional<EntrypointConnector> entrypointConnector = entrypointConnectors
            .stream()
            .filter(
                connector ->
                    connector.supportedListenerType() == ctx.getInternalAttribute(ATTR_INTERNAL_LISTENER_TYPE) && connector.matches(ctx)
            )
            .findFirst();
        return (T) entrypointConnector.orElse(null);
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        for (EntrypointConnector connector : entrypointConnectors) {
            try {
                connector.stop();
            } catch (Exception e) {
                log.warn("An error occurred when stopping entrypoint connector [{}].", connector.id());
            }
        }
    }

    @Override
    public DefaultEntrypointConnectorResolver preStop() throws Exception {
        super.preStop();

        for (EntrypointConnector connector : entrypointConnectors) {
            try {
                connector.preStop();
            } catch (Exception e) {
                log.warn("An error occurred when pre-stopping entrypoint connector [{}].", connector.id());
            }
        }

        return this;
    }
}
