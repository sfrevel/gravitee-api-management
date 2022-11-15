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
package io.gravitee.apim.gateway.tests.sdk;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.gravitee.apim.gateway.tests.sdk.utils.URLUtils.exchangePort;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.configuration.GatewayConfigurationBuilder;
import io.gravitee.apim.gateway.tests.sdk.plugin.PluginRegister;
import io.gravitee.apim.gateway.tests.sdk.runner.ApiConfigurer;
import io.gravitee.definition.model.Api;
import io.gravitee.definition.model.Endpoint;
import io.gravitee.gateway.platform.Organization;
import io.gravitee.gateway.platform.manager.OrganizationManager;
import io.gravitee.gateway.reactor.ReactableApi;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import io.reactivex.rxjava3.observers.TestObserver;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.PreconditionViolationException;
import org.mockito.Mockito;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

/**
 * @author Yann TAVERNIER (yann.tavernier at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractGatewayTest implements PluginRegister, ApiConfigurer, ApplicationContextAware {

    private int wiremockHttpsPort;
    private int wiremockPort;

    /**
     * Map of deployed apis for the current test method thanks to {@link io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi}
     */
    protected Map<String, ReactableApi<?>> deployedApis;
    private int gatewayPort = -1;
    private int technicalApiPort = -1;
    private Map<String, ReactableApi<?>> deployedForTestClass;
    private boolean areClassApisPrepared = false;
    protected ApplicationContext applicationContext;

    /**
     * The wiremock used by the deployed apis as a backend.
     */
    protected WireMockServer wiremock;

    /**
     * Override this method to modify the configuration of the wiremock server which acts as a backend for the deployed apis.
     * @param configuration the {@link WireMockConfiguration} to modify accordingly to your need.
     */
    protected void configureWireMock(WireMockConfiguration configuration) {}

    /**
     * Override this method if you want to pass some specific configuration to the gateway.
     * WARNING: since the gateway is run only once for the test class, this configuration will impact all your test cases.
     * To know the options, please check the documentation <a href="https://docs.gravitee.io/apim/3.x/apim_installguide_gateway_configuration.html#default_configuration">here</a>.
     * @param gatewayConfigurationBuilder is the configuration builder. Just use it with {@link GatewayConfigurationBuilder#set(String, Object)}.
     *                                    For example, to configure tags for the gateway, just use <code>gatewayConfigurationBuilder.set("tags", "my-tag")</code>
     */
    protected void configureGateway(GatewayConfigurationBuilder gatewayConfigurationBuilder) {}

    /**
     * Override this method if you want to pass some specific configuration to the HttpClient.
     * It differs from WebClient which only response with Single, which was not good for SSE api.
     */
    protected void configureHttpClient(HttpClientOptions options) {}

    /**
     * Proxy for {@link TestObserver#await(long, TimeUnit)} with a default of 30 seconds.
     * It awaits 30 seconds or until this TestObserver/TestSubscriber receives an onError or onComplete events, whichever happens first.
     * @param obs is the observer to await
     * @return the observer after wait
     */
    protected <T> TestObserver<T> awaitTerminalEvent(TestObserver<T> obs) throws InterruptedException {
        obs.await(30, TimeUnit.SECONDS);
        return obs;
    }

    /**
     * Get bean from Gateway container's  application context
     * Example: getBean(ApiKeyRepository.class);
     * @param requiredType is the type of the bean to get
     * @return the bean
     * @param <T> is the type of the bean to load.
     */
    protected <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    protected AbstractGatewayTest() {}

    @BeforeAll
    public static void init() {
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    }

    /**
     * Here we inject Vertx parameter even if it's not use:
     * when implementing the test cases, the developer will need a {@link io.vertx.ext.web.client.WebClient}, which is automatically resolved as a parameter if Vertx has already been resolved.
     * Injecting it in the BeforeEach at abstract class level allows to automatically inject it to ease the life of the developer.
     *
     * Ensure the testContext is completed before starting a test, see <a href="io.gravitee.gateway.standalone.flow>Vertx documentation</a>"
     * Update endpoints for each apis deployed for the whole test class, see {@link AbstractGatewayTest#updateEndpointsOnDeployedApisForClassIfNeeded()}
     * @param vertx this parameter is only used to let the VertxExtension initialize it. It will allow to use WebClient directly.
     * @param testContext
     */
    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) throws Exception {
        resetAllMocks();
        prepareWireMock();
        updateEndpointsOnDeployedApisForClassIfNeeded();
        updateEndpointsOnDeployedApisForTestIfNeeded();
        // Prepare something on a Vert.x event-loop thread
        // The thread changes with each test instance
        testContext.completeNow();
    }

    private void resetAllMocks() throws Exception {
        for (String name : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(name);
            if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
                bean = ((Advised) bean).getTargetSource().getTarget();
            }
            if (Mockito.mockingDetails(bean).isMock()) {
                Mockito.reset(bean);
            }
        }
    }

    private void prepareWireMock() {
        final WireMockConfiguration wireMockConfiguration = wireMockConfig().dynamicPort().dynamicHttpsPort();
        configureWireMock(wireMockConfiguration);
        // If a port has already been configured in a previous test, reuse the same for the class
        if (wiremockPort != -1) {
            wireMockConfiguration.port(wiremockPort);
        }
        if (wiremockHttpsPort != -1) {
            wireMockConfiguration.httpsPort(wiremockHttpsPort);
        }
        wiremock = new WireMockServer(wireMockConfiguration);
        wiremock.start();
        wiremockPort = wiremock.port();
        wiremockHttpsPort = wiremock.httpsPort();
    }

    /**
     * Ensure the testContext is completed after a test, see <a href="io.gravitee.gateway.standalone.flow>Vertx documentation</a>"
     * @param testContext
     */
    @AfterEach
    void cleanUp(VertxTestContext testContext) {
        // Clean things up on the same Vert.x event-loop thread
        // that called prepare and foo
        testContext.completeNow();
        wiremock.stop();
    }

    /**
     * HACK: To ease the developer life, we propose to configure {@link WireMockExtension} thanks to {@link RegisterExtension}.
     * Currently, there is no way to indicate to junit5 that {@link RegisterExtension} should be registered before a {@link org.junit.jupiter.api.extension.ExtendWith} one.
     * That said, our {@link GatewayTestingExtension} is registered before the wiremock server is configured, and apis for class levels are already deployed, but without the right wiremock port.
     * Doing that only once during the first {@link BeforeEach}, we are able to update the endpoints of apis declared at class level with {@link DeployApi}
     */
    private void updateEndpointsOnDeployedApisForClassIfNeeded() {
        if (!areClassApisPrepared && !deployedForTestClass.isEmpty()) {
            deployedForTestClass.forEach((k, v) -> updateEndpoints(v));
        }
        this.areClassApisPrepared = true;
    }

    private void updateEndpointsOnDeployedApisForTestIfNeeded() {
        if (!deployedApis.isEmpty()) {
            deployedApis.forEach((k, v) -> updateEndpoints(v));
        }
    }

    @Override
    public void configureApi(Api api) {}

    @Override
    public void configureApi(ReactableApi<?> api, Class<?> definitionClass) {}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Ensures the organization has the minimal requirement to be run properly.
     * - add a default id ("organization-id") if not set
     * @param organization to deploy
     */
    public void ensureMinimalRequirementForOrganization(Organization organization) {
        if (!StringUtils.hasText(organization.getId())) {
            organization.setId("organization-id");
        }
    }

    /**
     * Called by the {@link GatewayTestingExtension} when apis wanted for the test class are deployed.
     * @param deployedForTestClass is the list of deployed apis.
     */
    public void setDeployedClassApis(Map<String, ReactableApi<?>> deployedForTestClass) {
        this.deployedForTestClass = deployedForTestClass;
    }

    public int gatewayPort() {
        if (gatewayPort == -1) {
            gatewayPort = getAvailablePort();
        }
        return gatewayPort;
    }

    public int technicalApiPort() {
        if (technicalApiPort == -1) {
            technicalApiPort = getAvailablePort();
        }
        return technicalApiPort;
    }

    /**
     * Override api endpoints to replace port by the configured wiremock port.
     * Only valid for API Definition versions 1.0.0 and 2.0.0
     * @param reactableApi is the api to override
     */
    private void updateEndpoints(ReactableApi<?> reactableApi) {
        if (reactableApi.getDefinition() instanceof Api) {
            Api api = (Api) reactableApi.getDefinition();
            // Define dynamically endpoint port
            for (Endpoint endpoint : api.getProxy().getGroups().iterator().next().getEndpoints()) {
                if (endpoint.getTarget().contains("8080")) {
                    final int port = endpoint.getTarget().contains("https") ? wiremockHttpsPort : wiremockPort;
                    endpoint.setTarget(exchangePort(endpoint.getTarget(), port));
                }
            }
        }
    }

    /**
     * Override api endpoints port on demand
     * @param api is the api on which apply the new port
     * @param port is the port to reach.
     */
    protected void updateEndpointsPort(Api api, int port) {
        updateEndpoints(api, endpoint -> endpoint.setTarget(exchangePort(endpoint.getTarget(), port)));
    }

    /**
     * Override api endpoints
     * @param api is the api on which the endpoints will be transformed
     * @param endpointConsumer is the consumer used to transform the endpoints
     */
    protected void updateEndpoints(Api api, Consumer<Endpoint> endpointConsumer) {
        for (Endpoint endpoint : api.getProxy().getGroups().iterator().next().getEndpoints()) {
            endpointConsumer.accept(endpoint);
        }
    }

    /**
     * Update the current deployed organization (if it exists) and redeploy it.
     * Useful to add a policy for a specific test and avoid rewriting a json file.
     * @param organizationConsumer a consumer modifying the current deployed organization.
     */
    protected final void updateAndDeployOrganization(Consumer<Organization> organizationConsumer) {
        // Get deployed organization and create a new one from it
        final OrganizationManager organizationManager = applicationContext.getBean(OrganizationManager.class);
        final Organization currentOrganization = organizationManager.getCurrentOrganization();

        if (currentOrganization == null) {
            throw new PreconditionViolationException("No organization deployed, you cannot use this method");
        }

        Organization updatingOrganization = new Organization(currentOrganization);

        // Apply developer transformation on this organization
        organizationConsumer.accept(updatingOrganization);

        // redeploy new organization
        updatingOrganization.setUpdatedAt(new Date());
        organizationManager.register(updatingOrganization);
    }

    protected int getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
