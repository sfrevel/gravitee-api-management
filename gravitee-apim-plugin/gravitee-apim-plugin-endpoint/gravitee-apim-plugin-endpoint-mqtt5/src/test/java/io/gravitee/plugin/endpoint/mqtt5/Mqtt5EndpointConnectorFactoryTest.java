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
package io.gravitee.plugin.endpoint.mqtt5;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.gateway.jupiter.api.ApiType;
import io.gravitee.gateway.jupiter.api.ConnectorMode;
import io.gravitee.gateway.jupiter.api.connector.ConnectorHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public class Mqtt5EndpointConnectorFactoryTest {

    private Mqtt5EndpointConnectorFactory mqtt5EndpointConnectorFactory;

    @BeforeEach
    void beforeEach() {
        mqtt5EndpointConnectorFactory = new Mqtt5EndpointConnectorFactory(new ConnectorHelper(null, new ObjectMapper()));
    }

    @Test
    void shouldSupportAsyncApi() {
        assertThat(mqtt5EndpointConnectorFactory.supportedApi()).isEqualTo(ApiType.ASYNC);
    }

    @Test
    void shouldSupportSubscribePublishMode() {
        assertThat(mqtt5EndpointConnectorFactory.supportedModes()).contains(ConnectorMode.SUBSCRIBE, ConnectorMode.PUBLISH);
    }

    @ParameterizedTest
    @ValueSource(strings = { "wrong", "", "  ", "{\"unknown-key\":\"value\"}" })
    void shouldNotCreateConnectorWithWrongConfiguration(String configuration) {
        Mqtt5EndpointConnector connector = mqtt5EndpointConnectorFactory.createConnector(configuration);
        assertThat(connector).isNull();
    }

    @Test
    void shouldCreateConnectorWithRightConfiguration() {
        Mqtt5EndpointConnector connector = mqtt5EndpointConnectorFactory.createConnector(
            "{\"identifier\":\"identifier\",\"serverHost\":\"localhost\",\"serverPort\":\"1234\",\"topic\":\"test/topic\", \"consumer\":{}, \"producer\":{}}"
        );
        assertThat(connector).isNotNull();
        assertThat(connector.configuration).isNotNull();
        assertThat(connector.configuration.getIdentifier()).isEqualTo("identifier");
        assertThat(connector.configuration.getServerHost()).isEqualTo("localhost");
        assertThat(connector.configuration.getServerPort()).isEqualTo(1234);
        assertThat(connector.configuration.getTopic()).isEqualTo("test/topic");
        assertThat(connector.configuration.getConsumer()).isNotNull();
        assertThat(connector.configuration.getConsumer().isEnabled()).isTrue();
        assertThat(connector.configuration.getProducer()).isNotNull();
        assertThat(connector.configuration.getProducer().isEnabled()).isTrue();
    }

    @Test
    void shouldCreateConnectorWithEmptyConfiguration() {
        Mqtt5EndpointConnector connector = mqtt5EndpointConnectorFactory.createConnector("{}");
        assertThat(connector).isNotNull();
        assertThat(connector.configuration).isNotNull();
        assertThat(connector.configuration.getIdentifier()).isNull();
        assertThat(connector.configuration.getServerHost()).isNull();
        assertThat(connector.configuration.getServerPort()).isNull();
        assertThat(connector.configuration.getConsumer()).isNotNull();
        assertThat(connector.configuration.getProducer()).isNotNull();
    }

    @Test
    void shouldCreateConnectorWithNullConfiguration() {
        Mqtt5EndpointConnector connector = mqtt5EndpointConnectorFactory.createConnector(null);
        assertThat(connector).isNotNull();
        assertThat(connector.configuration).isNotNull();
        assertThat(connector.configuration.getIdentifier()).isNull();
        assertThat(connector.configuration.getServerHost()).isNull();
        assertThat(connector.configuration.getServerPort()).isNull();
        assertThat(connector.configuration.getConsumer()).isNotNull();
        assertThat(connector.configuration.getProducer()).isNotNull();
    }
}
