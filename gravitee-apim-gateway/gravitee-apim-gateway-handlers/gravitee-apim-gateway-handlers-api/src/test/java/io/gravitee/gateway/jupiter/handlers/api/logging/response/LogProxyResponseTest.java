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
package io.gravitee.gateway.jupiter.handlers.api.logging.response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.core.logging.LoggingContext;
import io.gravitee.gateway.core.logging.utils.LoggingUtils;
import io.gravitee.gateway.http.vertx.VertxHttpHeaders;
import io.gravitee.gateway.jupiter.api.context.HttpResponse;
import io.gravitee.gateway.jupiter.handlers.api.logging.LogHeadersCaptor;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
class LogProxyResponseTest {

    protected static final String URI = "https://test.gravitee.io";
    protected static final String BODY_CONTENT = "Body content";

    @Mock
    protected LoggingContext loggingContext;

    @Mock
    protected HttpResponse response;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(LoggingUtils.class, "EXCLUDED_CONTENT_TYPES_PATTERN", null);
    }

    @Test
    void shouldLogMethodAndUriOnly() {
        when(response.status()).thenReturn(HttpStatusCode.OK_200);
        when(loggingContext.proxyResponseHeaders()).thenReturn(false);
        when(loggingContext.proxyResponsePayload()).thenReturn(false);

        final LogProxyResponse logResponse = new LogProxyResponse(loggingContext, response);

        assertEquals(HttpStatusCode.OK_200, logResponse.getStatus());
        assertNull(logResponse.getHeaders());
        assertNull(logResponse.getBody());
    }

    @Test
    void shouldLogHeaders() {
        final HttpHeaders headers = new VertxHttpHeaders(new HeadersMultiMap());

        headers.set("X-Test1", "Value1");
        headers.set("X-Test2", "Value2");
        headers.set("X-Test3", List.of("Value3-a", "Value3-b"));

        when(response.headers()).thenReturn(headers);
        when(response.status()).thenReturn(HttpStatusCode.OK_200);
        when(loggingContext.proxyResponseHeaders()).thenReturn(true);
        when(loggingContext.proxyResponsePayload()).thenReturn(false);

        final LogProxyResponse logResponse = new LogProxyResponse(loggingContext, response);

        assertNotSame(headers, logResponse.getHeaders());
        assertTrue(logResponse.getHeaders().deeplyEquals(headers));
        assertNull(logResponse.getBody());
    }

    @Test
    void shouldLogNoHeadersWhenUsingLogHeadersCaptorAndNoHeadersHasChanged() {
        final VertxHttpHeaders existingHeaders = new VertxHttpHeaders(new HeadersMultiMap());
        existingHeaders.set("X-Test1", "Value1");
        existingHeaders.set("X-Test2", "Value2");
        existingHeaders.set("X-Test3", List.of("Value3-a", "Value3-b"));

        final HttpHeaders headers = new LogHeadersCaptor(existingHeaders);

        when(response.headers()).thenReturn(headers);
        when(response.status()).thenReturn(HttpStatusCode.OK_200);
        when(loggingContext.proxyResponseHeaders()).thenReturn(true);
        when(loggingContext.proxyResponsePayload()).thenReturn(false);

        final LogProxyResponse logResponse = new LogProxyResponse(loggingContext, response);

        assertNotSame(headers, logResponse.getHeaders());
        assertTrue(logResponse.getHeaders().deeplyEquals(HttpHeaders.create()));
        assertNull(logResponse.getBody());
    }

    @Test
    void shouldLogBody() {
        final Flowable<Buffer> body = Flowable.just(Buffer.buffer(BODY_CONTENT));
        final ArgumentCaptor<Flowable<Buffer>> chunksCaptor = ArgumentCaptor.forClass(Flowable.class);

        when(response.chunks()).thenReturn(body);
        when(response.headers()).thenReturn(HttpHeaders.create());
        when(loggingContext.proxyResponseHeaders()).thenReturn(false);
        when(loggingContext.proxyResponsePayload()).thenReturn(true);
        when(loggingContext.getMaxSizeLogMessage()).thenReturn(-1);

        final LogProxyResponse logResponse = new LogProxyResponse(loggingContext, response);
        verify(response).chunks(chunksCaptor.capture());

        final TestSubscriber<Buffer> obs = chunksCaptor.getValue().test();
        obs.assertComplete();

        assertNull(logResponse.getHeaders());
        assertEquals(BODY_CONTENT, logResponse.getBody());
    }

    @Test
    void shouldNotLogBodyWhenContentTypeExcluded() {
        final ArgumentCaptor<Flowable<Buffer>> chunksCaptor = ArgumentCaptor.forClass(Flowable.class);
        final HttpHeaders headers = HttpHeaders.create();
        headers.set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");

        when(response.headers()).thenReturn(headers);
        when(loggingContext.proxyResponseHeaders()).thenReturn(false);
        when(loggingContext.proxyResponsePayload()).thenReturn(true);
        when(loggingContext.getExcludedResponseTypes()).thenReturn("application/octet-stream");

        final LogProxyResponse logResponse = new LogProxyResponse(loggingContext, response);
        verify(response, times(0)).chunks(any(Flowable.class));

        assertNull(logResponse.getHeaders());
        assertNull(logResponse.getBody());
    }

    @Test
    void shouldLogPartialBodyWhenMaxPayloadSizeIsDefined() {
        final Flowable<Buffer> body = Flowable.just(Buffer.buffer(BODY_CONTENT));
        final int maxPayloadSize = 5;

        final ArgumentCaptor<Flowable<Buffer>> chunksCaptor = ArgumentCaptor.forClass(Flowable.class);
        when(response.chunks()).thenReturn(body);
        when(response.headers()).thenReturn(HttpHeaders.create());
        when(loggingContext.proxyResponseHeaders()).thenReturn(false);
        when(loggingContext.proxyResponsePayload()).thenReturn(true);
        when(loggingContext.getMaxSizeLogMessage()).thenReturn(maxPayloadSize);

        final LogProxyResponse logResponse = new LogProxyResponse(loggingContext, response);
        verify(response).chunks(chunksCaptor.capture());

        final TestSubscriber<Buffer> obs = chunksCaptor.getValue().test();
        obs.assertComplete();

        assertNull(logResponse.getHeaders());
        assertEquals(BODY_CONTENT.substring(0, maxPayloadSize), logResponse.getBody());
    }
}
