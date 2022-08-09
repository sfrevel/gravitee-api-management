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
package io.gravitee.plugin.endpoint.mock;

import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.jupiter.api.context.MessageExecutionContext;
import io.gravitee.gateway.jupiter.api.endpoint.async.EndpointAsyncConnector;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.gravitee.plugin.endpoint.mock.configuration.MockEndpointConnectorConfiguration;
import io.reactivex.Completable;
import io.reactivex.Flowable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author GraviteeSource Team
 */
public class MockEndpointConnector implements EndpointAsyncConnector {

    private MockEndpointConnectorConfiguration configuration;

    public MockEndpointConnector(String configurationString) {
        // TODO : read JSON configuration
        //this.configuration = mapper.read(configurationString);

        this.configuration = new MockEndpointConnectorConfiguration();
        configuration.setMessagesInterval(1L);
        configuration.setMessagesContent("test mock endpoint");
    }

    @Override
    public Completable connect(MessageExecutionContext ctx) {
        return sendDummyMessages(ctx);
    }

    private Completable sendDummyMessages(final MessageExecutionContext ctx) {
        return Completable.defer(
            () -> {
                ctx
                    .response()
                    .messages(
                        Flowable
                            .interval(configuration.getMessagesInterval(), TimeUnit.SECONDS)
                            .map(
                                value ->
                                    new Message() {
                                        @Override
                                        public HttpHeaders headers() {
                                            return null;
                                        }

                                        @Override
                                        public Map<String, Object> metadata() {
                                            return Map.of("interval", value);
                                        }

                                        @Override
                                        public Buffer content() {
                                            return Buffer.buffer(configuration.getMessagesContent() + " " + value);
                                        }
                                    }
                            )
                    );
                return Completable.complete();
            }
        );
    }
}
