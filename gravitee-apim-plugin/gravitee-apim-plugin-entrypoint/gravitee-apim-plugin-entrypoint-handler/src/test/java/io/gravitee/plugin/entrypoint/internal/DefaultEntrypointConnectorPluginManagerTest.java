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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.gateway.jupiter.api.connector.ConnectorHelper;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.EntrypointConnector;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.EntrypointConnectorConfiguration;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.EntrypointConnectorFactory;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPluginManager;
import io.gravitee.plugin.entrypoint.internal.fake.FakeEntrypointConnector;
import io.gravitee.plugin.entrypoint.internal.fake.FakeEntrypointConnectorFactory;
import io.gravitee.plugin.entrypoint.internal.fake.FakeEntrypointConnectorPlugin;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
class DefaultEntrypointConnectorPluginManagerTest {

    private static final String FAKE_ENTRYPOINT = "fake-entrypoint";

    private EntrypointConnectorPluginManager entrypointConnectorPluginManager;

    @BeforeEach
    public void beforeEach() {
        entrypointConnectorPluginManager =
            new DefaultEntrypointConnectorPluginManager(
                new DefaultEntrypointConnectorConnectorClassLoaderFactory(),
                new ConnectorHelper(null, new ObjectMapper())
            );
    }

    @Test
    void shouldRegisterNewEntrypointPluginWithoutConfiguration() {
        DefaultEntrypointConnectorPlugin entrypointPlugin = new DefaultEntrypointConnectorPlugin(
            new FakeEntrypointConnectorPlugin(),
            FakeEntrypointConnectorFactory.class,
            null
        );
        entrypointConnectorPluginManager.register(entrypointPlugin);
        EntrypointConnectorFactory<?> fake = entrypointConnectorPluginManager.getFactoryById("fake-entrypoint");
        assertThat(fake).isNotNull();
        EntrypointConnector fakeConnector = fake.createConnector(null);
        assertThat(fakeConnector).isNotNull();
    }

    @Test
    void shouldRegisterNewEntrypointPluginWithConfiguration() {
        DefaultEntrypointConnectorPlugin<FakeEntrypointConnectorFactory, EntrypointConnectorConfiguration> entrypointPlugin = new DefaultEntrypointConnectorPlugin(
            new FakeEntrypointConnectorPlugin(),
            FakeEntrypointConnectorFactory.class,
            EntrypointConnectorConfiguration.class
        );
        entrypointConnectorPluginManager.register(entrypointPlugin);
        EntrypointConnectorFactory<?> fake = entrypointConnectorPluginManager.getFactoryById("fake-entrypoint");
        assertThat(fake).isNotNull();
        EntrypointConnector fakeConnector = fake.createConnector("{\"info\":\"test\"}");
        assertThat(fakeConnector).isNotNull();
        assertThat(fakeConnector).isInstanceOf(FakeEntrypointConnector.class);
        FakeEntrypointConnector fakeEntrypointConnector = (FakeEntrypointConnector) fakeConnector;
        assertThat(fakeEntrypointConnector.getConfiguration().getInfo()).isEqualTo("test");
    }

    @Test
    void shouldNotRetrieveUnRegisterPlugin() {
        final EntrypointConnector factoryById = entrypointConnectorPluginManager.getFactoryById("fake-endpoint");
        assertThat(factoryById).isNull();
    }

    @Test
    void shouldNotFindSubscriptionSchemaFile() throws IOException {
        entrypointConnectorPluginManager.register(new FakeEntrypointConnectorPlugin(true));
        final String schema = entrypointConnectorPluginManager.getSubscriptionSchema(FAKE_ENTRYPOINT);
        assertThat(schema).isNull();
    }

    @Test
    void shouldGetFirstSubscriptionSchemaFile() throws IOException {
        entrypointConnectorPluginManager.register(new FakeEntrypointConnectorPlugin());
        final String schema = entrypointConnectorPluginManager.getSubscriptionSchema(FAKE_ENTRYPOINT);
        assertThat(schema).isEqualTo("{\n  \"schema\": \"subscription\"\n}");
    }
}
