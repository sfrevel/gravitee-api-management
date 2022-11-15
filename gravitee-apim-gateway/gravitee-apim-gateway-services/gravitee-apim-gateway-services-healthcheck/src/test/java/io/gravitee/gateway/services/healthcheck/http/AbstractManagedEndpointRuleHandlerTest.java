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
package io.gravitee.gateway.services.healthcheck.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.gravitee.common.http.HttpMethod;
import io.gravitee.definition.model.Endpoint;
import io.gravitee.definition.model.HttpClientOptions;
import io.gravitee.definition.model.endpoint.HttpEndpoint;
import io.gravitee.definition.model.services.healthcheck.HealthCheckRequest;
import io.gravitee.definition.model.services.healthcheck.HealthCheckResponse;
import io.gravitee.definition.model.services.healthcheck.HealthCheckStep;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.handlers.api.definition.Api;
import io.gravitee.gateway.services.healthcheck.EndpointRule;
import io.gravitee.reporter.api.health.EndpointStatus;
import io.gravitee.reporter.api.health.Step;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractManagedEndpointRuleHandlerTest {

    private Environment environment;

    @Mock
    private TemplateEngine templateEngine;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @BeforeEach
    void setup() {
        wm.resetAll();
        environment = mock(Environment.class);
        when(environment.getProperty("http.ssl.openssl", Boolean.class, false)).thenReturn(useOpenSsl());
    }

    protected abstract Boolean useOpenSsl();

    @Test
    void shouldNotValidate_invalidEndpoint(Vertx vertx, VertxTestContext context) throws Throwable {
        // Prepare HTTP endpoint
        wm.stubFor(get(urlEqualTo("/")).willReturn(notFound()));

        EndpointRule rule = createEndpointRule();

        HealthCheckStep step = new HealthCheckStep();
        HealthCheckRequest request = new HealthCheckRequest("/", HttpMethod.GET);

        step.setRequest(request);
        HealthCheckResponse response = new HealthCheckResponse();
        response.setAssertions(Collections.singletonList(HealthCheckResponse.DEFAULT_ASSERTION));
        step.setResponse(response);

        when(rule.steps()).thenReturn(Collections.singletonList(step));

        HttpEndpointRuleHandler runner = new HttpEndpointRuleHandler(vertx, rule, templateEngine, environment);

        // Verify
        runner.setStatusHandler(
            (Handler<EndpointStatus>) status -> {
                assertFalse(status.isSuccess());
                wm.verify(getRequestedFor(urlEqualTo("/")));
                context.completeNow();
            }
        );

        // Run
        runner.handle(null);

        // Wait until completion
        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS));
        assertTrue(context.completed());
    }

    @Test
    void shouldValidate(Vertx vertx, VertxTestContext context) throws Throwable {
        // Prepare HTTP endpoint
        wm.stubFor(get(urlEqualTo("/")).willReturn(ok("{\"status\": \"green\"}")));

        // Prepare
        EndpointRule rule = createEndpointRule();

        HealthCheckStep step = new HealthCheckStep();
        HealthCheckRequest request = new HealthCheckRequest("/", HttpMethod.GET);

        step.setRequest(request);
        HealthCheckResponse response = new HealthCheckResponse();
        response.setAssertions(Collections.singletonList(HealthCheckResponse.DEFAULT_ASSERTION));
        step.setResponse(response);
        when(rule.steps()).thenReturn(Collections.singletonList(step));

        HttpEndpointRuleHandler runner = new HttpEndpointRuleHandler(vertx, rule, templateEngine, environment);

        // Verify
        runner.setStatusHandler(
            (Handler<EndpointStatus>) status -> {
                assertTrue(status.isSuccess());
                wm.verify(getRequestedFor(urlEqualTo("/")));
                context.completeNow();
            }
        );

        // Run
        runner.handle(null);

        // Wait until completion
        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS));
        assertTrue(context.completed());
    }

    @Test
    void shouldNotValidate_invalidResponseBody(Vertx vertx, VertxTestContext context) throws Throwable {
        // Prepare HTTP endpoint
        wm.stubFor(get(urlEqualTo("/")).willReturn(ok("{\"status\": \"yellow\"}")));

        // Prepare
        EndpointRule rule = createEndpointRule();

        HealthCheckStep step = new HealthCheckStep();
        HealthCheckRequest request = new HealthCheckRequest("/", HttpMethod.GET);

        step.setRequest(request);
        HealthCheckResponse response = new HealthCheckResponse();
        response.setAssertions(Collections.singletonList("#jsonPath(#response.content, '$.status') == 'green'"));
        step.setResponse(response);
        when(rule.steps()).thenReturn(Collections.singletonList(step));

        HttpEndpointRuleHandler runner = new HttpEndpointRuleHandler(vertx, rule, templateEngine, environment);

        // Verify
        runner.setStatusHandler(
            (Handler<EndpointStatus>) status -> {
                assertFalse(status.isSuccess());
                wm.verify(getRequestedFor(urlEqualTo("/")));

                // When health-check is false, we store both request and response
                Step result = status.getSteps().get(0);
                assertEquals(HttpMethod.GET, result.getRequest().getMethod());
                assertNotNull(result.getResponse().getBody());

                context.completeNow();
            }
        );

        // Run
        runner.handle(null);

        // Wait until completion
        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS));
        assertTrue(context.completed());
    }

    @Test
    void shouldValidateFromRoot(Vertx vertx, VertxTestContext context) throws Throwable {
        // Prepare HTTP endpoint
        wm.stubFor(get(urlEqualTo("/")).willReturn(ok()));

        // Prepare
        EndpointRule rule = createEndpointRule("/additional-but-unused-path-for-hc");

        HealthCheckStep step = new HealthCheckStep();
        HealthCheckRequest request = new HealthCheckRequest("/", HttpMethod.GET);
        request.setFromRoot(true);

        step.setRequest(request);
        HealthCheckResponse response = new HealthCheckResponse();
        response.setAssertions(Collections.singletonList(HealthCheckResponse.DEFAULT_ASSERTION));
        step.setResponse(response);
        when(rule.steps()).thenReturn(Collections.singletonList(step));

        HttpEndpointRuleHandler runner = new HttpEndpointRuleHandler(vertx, rule, templateEngine, environment);

        // Verify
        runner.setStatusHandler(
            (Handler<EndpointStatus>) status -> {
                wm.verify(getRequestedFor(urlEqualTo("/")));
                wm.verify(0, getRequestedFor(urlEqualTo("/additional-but-unused-path-for-hc")));
                assertTrue(status.isSuccess());
                context.completeNow();
            }
        );

        // Run
        runner.handle(null);

        // Wait until completion
        assertTrue(context.awaitCompletion(5, TimeUnit.SECONDS));
        assertTrue(context.completed());
    }

    private Endpoint createEndpoint(String targetPath) {
        HttpEndpoint aDefault = new HttpEndpoint("default", "http://localhost:" + wm.getPort() + (targetPath != null ? targetPath : ""));
        aDefault.setHttpClientOptions(new HttpClientOptions());
        return aDefault;
    }

    private EndpointRule createEndpointRule() {
        return createEndpointRule(null);
    }

    private EndpointRule createEndpointRule(String targetPath) {
        EndpointRule rule = mock(EndpointRule.class);
        io.gravitee.definition.model.Api apiDefinition = new io.gravitee.definition.model.Api();
        apiDefinition.setId("an-api");
        Api api = new Api(apiDefinition);
        when(rule.endpoint()).thenReturn(createEndpoint(targetPath));
        when(rule.api()).thenReturn(api);
        when(rule.schedule()).thenReturn("0 */10 * ? * *");
        return rule;
    }
}
