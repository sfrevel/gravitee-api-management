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
package io.gravitee.plugin.endpoint.mqtt5.configuration;

import io.gravitee.gateway.jupiter.api.connector.endpoint.EndpointConnectorConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Mqtt5EndpointConnectorConfiguration implements EndpointConnectorConfiguration {

    private String identifier;
    private String serverHost;
    private Integer serverPort;
    private String topic;

    @Builder.Default
    private Consumer consumer = new Consumer();

    @Builder.Default
    private Producer producer = new Producer();

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Consumer {

        @Builder.Default
        private boolean enabled = true;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Producer {

        @Builder.Default
        private boolean enabled = true;

        @Builder.Default
        private boolean retained = false;

        @Builder.Default
        private long messageExpiryInterval = -1;

        private String responseTopic;
    }
}
