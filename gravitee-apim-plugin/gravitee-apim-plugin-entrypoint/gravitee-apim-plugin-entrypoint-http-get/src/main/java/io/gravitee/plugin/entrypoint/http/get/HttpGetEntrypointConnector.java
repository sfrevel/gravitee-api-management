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

import io.gravitee.common.http.HttpHeader;
import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.http.MediaType;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.jupiter.api.ConnectorMode;
import io.gravitee.gateway.jupiter.api.connector.entrypoint.async.EntrypointAsyncConnector;
import io.gravitee.gateway.jupiter.api.context.MessageExecutionContext;
import io.gravitee.gateway.jupiter.api.context.MessageRequest;
import io.gravitee.gateway.jupiter.api.message.DefaultMessage;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.gravitee.plugin.entrypoint.http.get.configuration.HttpGetEntrypointConnectorConfiguration;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@AllArgsConstructor
@Slf4j
public class HttpGetEntrypointConnector implements EntrypointAsyncConnector {

    static final Set<ConnectorMode> SUPPORTED_MODES = Set.of(ConnectorMode.SUBSCRIBE);

    protected final HttpGetEntrypointConnectorConfiguration configuration;

    @Override
    public Set<ConnectorMode> supportedModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public int matchCriteriaCount() {
        return 1;
    }

    @Override
    public boolean matches(final MessageExecutionContext ctx) {
        return (HttpMethod.GET == ctx.request().method());
    }

    @Override
    public Completable handleRequest(final MessageExecutionContext ctx) {
        return Completable.complete();
    }

    @Override
    public Completable handleResponse(final MessageExecutionContext ctx) {
        return Completable.defer(() -> collectAndFormatMessages(ctx).flatMapCompletable(buffer -> ctx.response().end(buffer)));
    }

    private Single<Buffer> collectAndFormatMessages(MessageExecutionContext ctx) {
        ctx.response().headers().add(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return ctx
            .response()
            .messages()
            .take(configuration.getMessageCount())
            .map(
                message -> {
                    JsonObject headers = JsonObject.mapFrom(message.headers());
                    Buffer content = message.content();
                    JsonObject metadata = JsonObject.mapFrom(message.metadata());

                    JsonObject jsonMessage = new JsonObject();
                    jsonMessage.put("headers", headers);
                    jsonMessage.put("content", content.toString());
                    jsonMessage.put("metadata", metadata);
                    return jsonMessage;
                }
            )
            .toList()
            .map(JsonArray::new)
            .map(objects -> Buffer.buffer(Json.encode(objects)));
    }
}
