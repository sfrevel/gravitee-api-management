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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.buffer.BufferFactory;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.jupiter.api.context.MessageExecutionContext;
import io.gravitee.gateway.jupiter.api.context.MessageRequest;
import io.gravitee.gateway.jupiter.api.context.MessageResponse;
import io.gravitee.gateway.jupiter.api.message.DefaultMessage;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.gravitee.plugin.entrypoint.http.get.configuration.HttpGetEntrypointConnectorConfiguration;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
class HttpGetEntrypointConnectorTest {

    @Mock
    private MessageExecutionContext mockMessageExecutionContext;

    @Mock
    private MessageRequest mockMessageRequest;

    @Mock
    private HttpGetEntrypointConnectorConfiguration configuration;

    private HttpGetEntrypointConnector httpGetEntrypointConnector;

    @BeforeEach
    void beforeEach() {
        httpGetEntrypointConnector = new HttpGetEntrypointConnector(configuration);
    }

    @Test
    void shouldMatchesCriteriaReturnValidCount() {
        assertThat(httpGetEntrypointConnector.matchCriteriaCount()).isEqualTo(1);
    }

    @Test
    void shouldMatchesWithValidContext() {
        when(mockMessageRequest.method()).thenReturn(HttpMethod.GET);
        when(mockMessageExecutionContext.request()).thenReturn(mockMessageRequest);

        boolean matches = httpGetEntrypointConnector.matches(mockMessageExecutionContext);

        assertThat(matches).isTrue();
    }

    @Test
    void shouldNotMatchesWithBadMethod() {
        when(mockMessageRequest.method()).thenReturn(HttpMethod.POST);
        when(mockMessageExecutionContext.request()).thenReturn(mockMessageRequest);

        boolean matches = httpGetEntrypointConnector.matches(mockMessageExecutionContext);

        assertThat(matches).isFalse();
    }

    @Test
    void shouldDoNothingOnRequest() {
        httpGetEntrypointConnector.handleRequest(mockMessageExecutionContext).test().assertComplete();
        verifyNoInteractions(mockMessageExecutionContext);
    }

    @Test
    void shouldCompleteAndEndWhenResponseMessagesComplete() {
        Flowable<Message> empty = Flowable.empty();
        DummyMessageResponse dummyMessageResponse = new DummyMessageResponse();
        dummyMessageResponse.messages(empty);
        when(mockMessageExecutionContext.response()).thenReturn(dummyMessageResponse);

        httpGetEntrypointConnector.handleResponse(mockMessageExecutionContext).test().assertComplete();

        assertThat(dummyMessageResponse.headers()).isNotNull();
        assertThat(dummyMessageResponse.headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(Json.decodeValue(dummyMessageResponse.getBuffer().toString())).isEqualTo(new JsonArray());
    }

    @Test
    void shouldWriteArrayOfMessageAndCompleteAndEndWhenResponseMessagesCompleteWhenReadingLessMessageThanAvailable() {
        Flowable<Message> messages = Flowable.just(createMessage("1"), createMessage("2"), createMessage("3"));
        DummyMessageResponse dummyMessageResponse = new DummyMessageResponse();
        dummyMessageResponse.messages(messages);
        when(mockMessageExecutionContext.response()).thenReturn(dummyMessageResponse);

        when(configuration.getMessageCount()).thenReturn(2L);

        httpGetEntrypointConnector.handleResponse(mockMessageExecutionContext).test().assertComplete();

        JsonArray actual = (JsonArray) Json.decodeValue(dummyMessageResponse.getBuffer().toString());
        assertThat(actual.size()).isEqualTo(2);
        testResponseItem(actual.getJsonObject(0), "1");
        testResponseItem(actual.getJsonObject(1), "2");
    }

    @Test
    void shouldWriteArrayOfMessageAndCompleteAndEndWhenResponseMessagesCompleteWhenReadingMoreMessageThanAvailable() {
        Flowable<Message> messages = Flowable.just(createMessage("1"), createMessage("2"), createMessage("3"));
        DummyMessageResponse dummyMessageResponse = new DummyMessageResponse();
        dummyMessageResponse.messages(messages);
        when(mockMessageExecutionContext.response()).thenReturn(dummyMessageResponse);

        when(configuration.getMessageCount()).thenReturn(4L);

        httpGetEntrypointConnector.handleResponse(mockMessageExecutionContext).test().assertComplete();

        JsonArray actual = (JsonArray) Json.decodeValue(dummyMessageResponse.getBuffer().toString());
        assertThat(actual.size()).isEqualTo(3);
        testResponseItem(actual.getJsonObject(0), "1");
        testResponseItem(actual.getJsonObject(1), "2");
        testResponseItem(actual.getJsonObject(2), "3");
    }

    private void testResponseItem(JsonObject item, String content) {
        assertThat(item.getJsonObject("headers")).isNotNull();
        assertThat(item.getJsonObject("headers").getJsonArray("X-My-Header-" + content)).isNotNull();
        assertThat(item.getJsonObject("headers").getJsonArray("X-My-Header-" + content).size()).isEqualTo(1);
        assertThat(item.getJsonObject("headers").getJsonArray("X-My-Header-" + content).getString(0)).isEqualTo("headerValue" + content);
        assertThat(item.getString("content")).isNotNull();
        assertThat(item.getString("content")).isEqualTo(content);
        assertThat(item.getJsonObject("metadata")).isNotNull();
        assertThat(item.getJsonObject("metadata").getString("myKey")).isNotNull();
        assertThat(item.getJsonObject("metadata").getString("myKey")).isEqualTo("myValue" + content);
    }

    private Message createMessage(String messageContent) {
        return DefaultMessage
            .builder()
            .headers(HttpHeaders.create().set("X-My-Header-" + messageContent, "headerValue" + messageContent))
            .content(messageContent.getBytes())
            .metadata(Map.of("myKey", "myValue" + messageContent))
            .build();
    }
}
