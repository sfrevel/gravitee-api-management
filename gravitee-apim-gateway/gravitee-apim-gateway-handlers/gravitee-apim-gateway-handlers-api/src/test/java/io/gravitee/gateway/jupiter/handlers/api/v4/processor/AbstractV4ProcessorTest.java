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
package io.gravitee.gateway.jupiter.handlers.api.v4.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

import io.gravitee.definition.model.v4.Api;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.core.component.CustomComponentProvider;
import io.gravitee.gateway.jupiter.core.context.MutableRequest;
import io.gravitee.gateway.jupiter.core.context.MutableResponse;
import io.gravitee.gateway.jupiter.handlers.api.processor.AbstractProcessorTest;
import io.gravitee.gateway.jupiter.reactor.handler.context.DefaultExecutionContext;
import io.gravitee.reporter.api.http.Metrics;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
public class AbstractV4ProcessorTest extends AbstractProcessorTest {

    protected Api api;

    @Mock
    protected Metrics mockMetrics;

    @Mock
    protected MutableRequest mockRequest;

    protected HttpHeaders spyRequestHeaders;

    @Spy
    protected MutableResponse mockResponse;

    protected HttpHeaders spyResponseHeaders;

    protected DefaultExecutionContext ctx;
    protected CustomComponentProvider componentProvider;

    @BeforeEach
    public void init() {
        spyRequestHeaders = spy(HttpHeaders.create());
        spyResponseHeaders = spy(HttpHeaders.create());
        lenient().when(mockRequest.metrics()).thenReturn(mockMetrics);
        lenient().when(mockRequest.headers()).thenReturn(spyRequestHeaders);
        lenient().when(mockResponse.headers()).thenReturn(spyResponseHeaders);
        lenient().when(mockRequest.messages()).thenReturn(Flowable.empty());
        lenient().when(mockRequest.onMessage(any())).thenReturn(Completable.complete());
        lenient().when(mockRequest.onMessages(any())).thenReturn(Completable.complete());
        lenient().when(mockResponse.messages()).thenReturn(Flowable.empty());
        lenient().when(mockResponse.onMessage(any())).thenReturn(Completable.complete());
        lenient().when(mockResponse.onMessages(any())).thenReturn(Completable.complete());
        lenient().when(mockResponse.chunks()).thenReturn(Flowable.empty());
        api = new Api();
        componentProvider = new CustomComponentProvider();
        componentProvider.add(Api.class, api);
        ctx = new DefaultExecutionContext(mockRequest, mockResponse);
        ctx.componentProvider(componentProvider);
    }
}
