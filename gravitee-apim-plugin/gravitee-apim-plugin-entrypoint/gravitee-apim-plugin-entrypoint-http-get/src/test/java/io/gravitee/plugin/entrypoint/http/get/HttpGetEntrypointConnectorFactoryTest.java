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

import static org.assertj.core.api.Assertions.assertThat;

import io.gravitee.gateway.jupiter.api.ConnectorMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
class HttpGetEntrypointConnectorFactoryTest {

    private HttpGetEntrypointConnectorFactory httpGetEntrypointConnectorFactory;

    @BeforeEach
    void beforeEach() {
        httpGetEntrypointConnectorFactory = new HttpGetEntrypointConnectorFactory();
    }

    @Test
    void shouldSupportSubscribeMode() {
        assertThat(httpGetEntrypointConnectorFactory.supportedModes()).contains(ConnectorMode.SUBSCRIBE);
    }

    @ParameterizedTest
    @ValueSource(strings = { "wrong", "", "  " })
    void shouldCreateConnectorWithWrongConfiguration(String configuration) {
        HttpGetEntrypointConnector connector = httpGetEntrypointConnectorFactory.createConnector(configuration);
        assertThat(connector).isNull();
    }

    @Test
    void shouldCreateConnectorWithNullConfiguration() {
        HttpGetEntrypointConnector connector = httpGetEntrypointConnectorFactory.createConnector(null);
        assertThat(connector).isNotNull();
        assertThat(connector.configuration).isNotNull();
        assertThat(connector.configuration.getMessageCount()).isEqualTo(500);
    }
}
