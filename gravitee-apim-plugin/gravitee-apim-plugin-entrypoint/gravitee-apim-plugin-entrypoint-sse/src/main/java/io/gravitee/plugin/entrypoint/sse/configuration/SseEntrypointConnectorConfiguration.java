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
package io.gravitee.plugin.entrypoint.sse.configuration;

import io.gravitee.gateway.jupiter.api.connector.entrypoint.EntrypointConnectorConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@Getter
@Setter
public class SseEntrypointConnectorConfiguration implements EntrypointConnectorConfiguration {

    /**
     * Define the interval in which heartbeat are sent to client.
     */
    private int heartbeatIntervalInMs = 5_000;

    /**
     * Allow sending messages metadata to client as SSE comments.
     */
    private boolean metadataAsComment = false;

    /**
     * Allow sending messages headers to client as SSE comments.
     */
    private boolean headersAsComment = false;
}
