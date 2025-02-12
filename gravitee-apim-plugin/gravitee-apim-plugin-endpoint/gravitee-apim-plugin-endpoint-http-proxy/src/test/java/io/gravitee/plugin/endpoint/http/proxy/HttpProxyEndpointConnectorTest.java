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
package io.gravitee.plugin.endpoint.http.proxy;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.gravitee.gateway.api.http.HttpHeaderNames.*;
import static io.gravitee.plugin.endpoint.http.proxy.HttpProxyEndpointConnector.HOP_HEADERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.gravitee.common.http.HttpHeader;
import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.http.vertx.VertxHttpHeaders;
import io.gravitee.gateway.jupiter.api.ApiType;
import io.gravitee.gateway.jupiter.api.ConnectorMode;
import io.gravitee.gateway.jupiter.api.context.DeploymentContext;
import io.gravitee.gateway.jupiter.api.context.ExecutionContext;
import io.gravitee.gateway.jupiter.api.context.Request;
import io.gravitee.gateway.jupiter.api.context.Response;
import io.gravitee.plugin.endpoint.http.proxy.client.VertxHttpClientHelper;
import io.gravitee.plugin.endpoint.http.proxy.configuration.HttpProxyEndpointConnectorConfiguration;
import io.gravitee.reporter.api.http.Metrics;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpClient;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
class HttpProxyEndpointConnectorTest {

    protected static final String REQUEST_BODY = "Post body content";
    protected static final String REQUEST_BODY_CHUNK1 = "Post ";
    protected static final String REQUEST_BODY_CHUNK2 = "body chunk ";
    protected static final String REQUEST_BODY_CHUNK3 = "content";

    protected static final int REQUEST_BODY_LENGTH = REQUEST_BODY.getBytes().length;
    protected static final String BACKEND_RESPONSE_BODY = "response from backend";
    public static final int TIMEOUT_SECONDS = 60;
    private static WireMockServer wiremock;
    private static Vertx vertx;

    @Mock
    private DeploymentContext deploymentCtx;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    private Metrics metrics;

    @Mock
    private MockedStatic<VertxHttpClientHelper> vertxHttpClientHelperMockedStatic;

    private HttpClient httpClient;
    private HttpHeaders requestHeaders;
    private HttpHeaders responseHeaders;
    private HttpProxyEndpointConnectorConfiguration configuration;
    private HttpProxyEndpointConnector cut;

    private AtomicInteger httpClientCreationCount;

    @BeforeAll
    static void setup() {
        final WireMockConfiguration wireMockConfiguration = wireMockConfig().dynamicPort().dynamicHttpsPort();
        wiremock = new WireMockServer(wireMockConfiguration);
        wiremock.start();
        vertx = Vertx.vertx();
    }

    @AfterAll
    static void tearDown() {
        wiremock.stop();
        wiremock.shutdownServer();
        vertx.close().blockingAwait(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @BeforeEach
    void init() {
        final WireMockConfiguration wireMockConfiguration = wireMockConfig().dynamicPort().dynamicHttpsPort();
        wiremock = new WireMockServer(wireMockConfiguration);
        wiremock.start();

        final HttpClientOptions httpClientOptions = new HttpClientOptions().setDefaultPort(wiremock.port()).setDefaultHost("localhost");
        httpClient = spy(vertx.createHttpClient(httpClientOptions));

        httpClientCreationCount = new AtomicInteger(0);
        lenient().when(deploymentCtx.getTemplateEngine()).thenReturn(templateEngine);

        vertxHttpClientHelperMockedStatic
            .when(() -> VertxHttpClientHelper.buildHttpClient(any(), any(), any(), anyString()))
            .thenAnswer(
                invocation -> {
                    httpClientCreationCount.incrementAndGet();
                    vertxHttpClientHelperMockedStatic.close();
                    return httpClient;
                }
            );

        lenient().when(ctx.request()).thenReturn(request);
        lenient().when(ctx.response()).thenReturn(response);

        requestHeaders = HttpHeaders.create();
        lenient().when(request.pathInfo()).thenReturn("");
        lenient().when(request.headers()).thenReturn(requestHeaders);
        lenient().when(request.metrics()).thenReturn(metrics);
        lenient().when(request.chunks()).thenReturn(Flowable.empty());

        responseHeaders = HttpHeaders.create();
        lenient().when(response.headers()).thenReturn(responseHeaders);

        configuration = new HttpProxyEndpointConnectorConfiguration();

        configuration.setTarget("http://localhost:" + wiremock.port() + "/team");
        cut = new HttpProxyEndpointConnector(configuration);
    }

    @AfterEach
    void cleanUp() {
        if (!vertxHttpClientHelperMockedStatic.isClosed()) {
            vertxHttpClientHelperMockedStatic.close();
        }

        wiremock.resetAll();
        httpClient.close().blockingAwait(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void shouldSupportSyncApi() {
        assertThat(cut.supportedApi()).isEqualTo(ApiType.SYNC);
    }

    @Test
    void shouldSupportRequestResponseModes() {
        assertThat(cut.supportedModes()).containsOnly(ConnectorMode.REQUEST_RESPONSE);
    }

    @Test
    void shouldReturnHttpProxyId() {
        assertThat(cut.id()).isEqualTo("http-proxy");
    }

    @Test
    void shouldExecuteGetRequest() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();

        assertNoTimeout(obs);
        obs.assertComplete();

        wiremock.verify(1, getRequestedFor(urlPathEqualTo("/team")));
    }

    @Test
    void shouldExecutePostRequest() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.POST);
        when(request.chunks())
            .thenReturn(
                Flowable.just(Buffer.buffer(REQUEST_BODY_CHUNK1), Buffer.buffer(REQUEST_BODY_CHUNK2), Buffer.buffer(REQUEST_BODY_CHUNK3))
            );

        wiremock.stubFor(post(urlPathEqualTo("/team")).willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        wiremock.verify(
            1,
            postRequestedFor(urlPathEqualTo("/team"))
                .withHeader(TRANSFER_ENCODING, new EqualToPattern("chunked"))
                .withRequestBody(new EqualToPattern(REQUEST_BODY_CHUNK1 + REQUEST_BODY_CHUNK2 + REQUEST_BODY_CHUNK3))
        );
    }

    @Test
    void shouldExecutePostRequestChunked() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.POST);
        when(request.chunks()).thenReturn(Flowable.just(Buffer.buffer(REQUEST_BODY)));
        requestHeaders.set(CONTENT_LENGTH, "" + REQUEST_BODY_LENGTH);

        wiremock.stubFor(post("/team").withRequestBody(new EqualToPattern(REQUEST_BODY)).willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        wiremock.verify(
            1,
            postRequestedFor(urlPathEqualTo("/team"))
                .withHeader(CONTENT_LENGTH, new EqualToPattern("" + REQUEST_BODY_LENGTH))
                .withRequestBody(new EqualToPattern(REQUEST_BODY))
        );
    }

    @Test
    void shouldPropagateRequestHeadersAndRemoveHopHeaders() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());

        requestHeaders.add("X-Custom", List.of("value1", "value2"));
        HOP_HEADERS.forEach(header -> requestHeaders.add(header.toString(), "should be removed"));

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        RequestPatternBuilder requestPatternBuilder = getRequestedFor(urlPathEqualTo("/team"))
            .withHeader("X-Custom", new EqualToPattern("value1"))
            .withHeader("X-Custom", new EqualToPattern("value2"));

        for (CharSequence header : HOP_HEADERS) {
            requestPatternBuilder = requestPatternBuilder.withoutHeader(header.toString());
        }

        wiremock.verify(1, requestPatternBuilder);
    }

    @Test
    void shouldAddOrReplaceRequestHeadersWithConfiguration() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());
        configuration.setHeaders(List.of(new HttpHeader("X-To-Be-Overriden", "Override"), new HttpHeader("X-To-Be-Added", "Added")));

        requestHeaders.add("X-Custom", "value1");
        requestHeaders.add("X-To-Be-Overriden", List.of("toOverrideValue1", "toOverrideValue2"));

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        RequestPatternBuilder requestPatternBuilder = getRequestedFor(urlPathEqualTo("/team"))
            .withHeader("X-Custom", new EqualToPattern("value1"))
            .withHeader("X-To-Be-Overriden", new EqualToPattern("Override"))
            .withHeader("X-To-Be-Added", new EqualToPattern("Added"));

        for (CharSequence header : HOP_HEADERS) {
            requestPatternBuilder = requestPatternBuilder.withoutHeader(header.toString());
        }

        wiremock.verify(1, requestPatternBuilder);
    }

    @Test
    void shouldOverrideHostWithRequestHostHeader() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());
        when(request.originalHost()).thenReturn("localhost:8082");

        // Simulated a policy that force the host header to use when calling the backend endpoint.
        when(request.host()).thenReturn("api.gravitee.io");
        requestHeaders.add("X-Custom", "value1");

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        RequestPatternBuilder requestPatternBuilder = getRequestedFor(urlPathEqualTo("/team"))
            .withHeader(HOST, new EqualToPattern("api.gravitee.io"))
            .withHeader("X-Custom", new EqualToPattern("value1"));

        for (CharSequence header : HOP_HEADERS) {
            requestPatternBuilder = requestPatternBuilder.withoutHeader(header.toString());
        }

        wiremock.verify(1, requestPatternBuilder);
    }

    @Test
    void shouldNotOverrideRequestHostHeaderWhenSameAsRequestOriginalHost() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());
        when(request.originalHost()).thenReturn("api.gravitee.io");
        when(request.host()).thenReturn("api.gravitee.io");

        requestHeaders.add("X-Custom", "value1");

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        RequestPatternBuilder requestPatternBuilder = getRequestedFor(urlPathEqualTo("/team"))
            .withHeader(HOST, new EqualToPattern("localhost:" + wiremock.port()))
            .withHeader("X-Custom", new EqualToPattern("value1"));

        for (CharSequence header : HOP_HEADERS) {
            requestPatternBuilder = requestPatternBuilder.withoutHeader(header.toString());
        }

        wiremock.verify(1, requestPatternBuilder);
    }

    @Test
    void shouldPropagateRequestVertxHttpHeaderWithoutTemporaryCopy() throws InterruptedException {
        requestHeaders = new VertxHttpHeaders(new HeadersMultiMap());
        when(request.headers()).thenReturn(requestHeaders);
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());

        requestHeaders.add("X-Custom", List.of("value1", "value2"));
        HOP_HEADERS.forEach(header -> requestHeaders.add(header.toString(), "should be removed"));

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        RequestPatternBuilder requestPatternBuilder = getRequestedFor(urlPathEqualTo("/team"))
            .withHeader("X-Custom", new EqualToPattern("value1"))
            .withHeader("X-Custom", new EqualToPattern("value2"));

        for (CharSequence header : HOP_HEADERS) {
            requestPatternBuilder = requestPatternBuilder.withoutHeader(header.toString());
        }

        wiremock.verify(1, requestPatternBuilder);
    }

    @Test
    void shouldPropagateResponseHeaders() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());

        wiremock.stubFor(
            get("/team")
                .willReturn(
                    ok(BACKEND_RESPONSE_BODY).withHeader("X-Response-Header", "Value1", "Value2").withHeader("X-Other", "OtherValue")
                )
        );

        final TestObserver<Void> obs = cut.connect(ctx).test();

        assertNoTimeout(obs);
        obs.assertComplete();

        assertEquals(List.of("Value1", "Value2"), responseHeaders.getAll("X-Response-Header"));
        assertEquals(List.of("OtherValue"), responseHeaders.getAll("X-Other"));
        wiremock.verify(1, getRequestedFor(urlPathEqualTo("/team")));
    }

    @Test
    void shouldPropagateResponseHeadersWhenVertxResponseHeader() throws InterruptedException {
        responseHeaders = new VertxHttpHeaders(new HeadersMultiMap());

        when(response.headers()).thenReturn(responseHeaders);
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());

        wiremock.stubFor(
            get("/team")
                .willReturn(
                    ok(BACKEND_RESPONSE_BODY).withHeader("X-Response-Header", "Value1", "Value2").withHeader("X-Other", "OtherValue")
                )
        );

        final TestObserver<Void> obs = cut.connect(ctx).test();

        assertNoTimeout(obs);
        obs.assertComplete();

        assertEquals(List.of("Value1", "Value2"), responseHeaders.getAll("X-Response-Header"));
        assertEquals(List.of("OtherValue"), responseHeaders.getAll("X-Other"));
        wiremock.verify(1, getRequestedFor(urlPathEqualTo("/team")));
    }

    @Test
    void shouldInstantiateHttpClientOnce() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        for (int i = 0; i < TIMEOUT_SECONDS; i++) {
            final TestObserver<Void> obs = cut.connect(ctx).test();

            assertNoTimeout(obs);
            obs.assertComplete();
        }

        wiremock.verify(TIMEOUT_SECONDS, getRequestedFor(urlPathEqualTo("/team")));
        assertEquals(1, httpClientCreationCount.get());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWithNullTarget() {
        configuration.setTarget(null);

        assertThrows(IllegalArgumentException.class, () -> cut = new HttpProxyEndpointConnector(configuration));
    }

    @Test
    void shouldExecuteRequestWithQueryParameters() throws InterruptedException {
        final LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("foo1", "bar1");
        parameters.add("foo2", "bar2");

        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());
        when(request.parameters()).thenReturn(parameters);

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        wiremock.verify(
            1,
            getRequestedFor(urlPathEqualTo("/team"))
                .withQueryParam("foo1", new EqualToPattern("bar1"))
                .withQueryParam("foo2", new EqualToPattern("bar2"))
        );
    }

    @Test
    void shouldExecuteRequestWithQueryParametersMergedWithTargetQueryParams() throws InterruptedException {
        final LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("foo1", "bar1");
        parameters.add("foo2", "bar2");
        parameters.add("foo3", null);

        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());
        when(request.parameters()).thenReturn(parameters);

        configuration.setTarget("http://localhost:" + wiremock.port() + "/team?param1=value1&param2=value2");
        cut = new HttpProxyEndpointConnector(configuration);

        wiremock.stubFor(get(urlPathEqualTo("/team")).willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();
        assertNoTimeout(obs);
        obs.assertComplete();

        wiremock.verify(
            1,
            getRequestedFor(urlPathEqualTo("/team"))
                .withQueryParam("foo1", new EqualToPattern("bar1"))
                .withQueryParam("foo2", new EqualToPattern("bar2"))
                .withQueryParam("param1", new EqualToPattern("value1"))
                .withQueryParam("param2", new EqualToPattern("value2"))
                .withQueryParam("foo3", new EqualToPattern(""))
        );
    }

    @Test
    void shouldErrorWhenExceptionIsThrown() {
        configuration.setTarget("http://localhost:" + wiremock.port() + "/team");

        cut = new HttpProxyEndpointConnector(configuration);

        final TestObserver<Void> obs = cut.connect(ctx).test();
        obs.assertError(NullPointerException.class);
    }

    @Test
    void shouldStopHttpClientWhenStopping() throws InterruptedException {
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.chunks()).thenReturn(Flowable.empty());

        wiremock.stubFor(get("/team").willReturn(ok(BACKEND_RESPONSE_BODY)));

        final TestObserver<Void> obs = cut.connect(ctx).test();

        assertNoTimeout(obs);
        obs.assertComplete();

        cut.doStop();
        verify(httpClient).close();
    }

    @Test
    void shouldNotStopHttpClientIfNotCreated() {
        cut.doStop();

        verify(httpClient, never()).close();
    }

    private void assertNoTimeout(TestObserver<Void> obs) throws InterruptedException {
        assertThat(obs.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue().as("Should complete before timeout");
    }
}
