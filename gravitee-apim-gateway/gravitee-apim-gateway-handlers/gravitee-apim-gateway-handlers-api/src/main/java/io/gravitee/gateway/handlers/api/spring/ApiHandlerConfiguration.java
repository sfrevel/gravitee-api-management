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
package io.gravitee.gateway.handlers.api.spring;

import io.gravitee.common.util.DataEncryptor;
import io.gravitee.gateway.core.classloader.DefaultClassLoader;
import io.gravitee.gateway.core.component.ComponentProvider;
import io.gravitee.gateway.core.component.spring.SpringComponentProvider;
import io.gravitee.gateway.core.condition.ExpressionLanguageStringConditionEvaluator;
import io.gravitee.gateway.env.HttpRequestTimeoutConfiguration;
import io.gravitee.gateway.handlers.api.ApiReactorHandlerFactory;
import io.gravitee.gateway.handlers.api.definition.Api;
import io.gravitee.gateway.handlers.api.manager.ApiManager;
import io.gravitee.gateway.handlers.api.manager.endpoint.ApiManagementEndpoint;
import io.gravitee.gateway.handlers.api.manager.endpoint.ApisManagementEndpoint;
import io.gravitee.gateway.handlers.api.manager.endpoint.NodeApisEndpointInitializer;
import io.gravitee.gateway.handlers.api.manager.impl.ApiManagerImpl;
import io.gravitee.gateway.handlers.api.services.ApiKeyService;
import io.gravitee.gateway.handlers.api.services.SubscriptionService;
import io.gravitee.gateway.jupiter.core.condition.CompositeConditionFilter;
import io.gravitee.gateway.jupiter.core.condition.ExpressionLanguageConditionFilter;
import io.gravitee.gateway.jupiter.core.condition.ExpressionLanguageMessageConditionFilter;
import io.gravitee.gateway.jupiter.flow.condition.evaluation.HttpMethodConditionFilter;
import io.gravitee.gateway.jupiter.flow.condition.evaluation.PathBasedConditionFilter;
import io.gravitee.gateway.jupiter.handlers.api.processor.ApiProcessorChainFactory;
import io.gravitee.gateway.jupiter.handlers.api.v4.AsyncReactorFactory;
import io.gravitee.gateway.jupiter.handlers.api.v4.flow.resolver.FlowResolverFactory;
import io.gravitee.gateway.jupiter.handlers.api.v4.processor.ApiMessageProcessorChainFactory;
import io.gravitee.gateway.jupiter.policy.DefaultPolicyFactory;
import io.gravitee.gateway.jupiter.policy.PolicyChainFactory;
import io.gravitee.gateway.jupiter.policy.PolicyFactory;
import io.gravitee.gateway.jupiter.reactor.v4.reactor.ReactorFactory;
import io.gravitee.gateway.jupiter.reactor.v4.subscription.SubscriptionDispatcher;
import io.gravitee.gateway.jupiter.v4.flow.selection.ChannelSelectorConditionFilter;
import io.gravitee.gateway.jupiter.v4.flow.selection.ConditionSelectorConditionFilter;
import io.gravitee.gateway.jupiter.v4.flow.selection.HttpSelectorConditionFilter;
import io.gravitee.gateway.platform.manager.OrganizationManager;
import io.gravitee.gateway.policy.PolicyChainProviderLoader;
import io.gravitee.gateway.policy.PolicyPluginFactory;
import io.gravitee.gateway.policy.impl.PolicyFactoryCreatorImpl;
import io.gravitee.gateway.policy.impl.PolicyPluginFactoryImpl;
import io.gravitee.gateway.reactor.handler.context.ApiTemplateVariableProviderFactory;
import io.gravitee.node.api.Node;
import io.gravitee.node.api.cache.CacheManager;
import io.gravitee.plugin.endpoint.EndpointConnectorPluginManager;
import io.gravitee.plugin.entrypoint.EntrypointConnectorPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class ApiHandlerConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Node node;

    @Autowired
    private io.gravitee.node.api.configuration.Configuration configuration;

    @Bean
    public ApiManager apiManager() {
        return new ApiManagerImpl();
    }

    @Bean
    public DefaultClassLoader classLoader() {
        return new DefaultClassLoader(this.getClass().getClassLoader());
    }

    @Bean
    public ApisManagementEndpoint apisManagementEndpoint() {
        return new ApisManagementEndpoint();
    }

    @Bean
    public ApiManagementEndpoint apiManagementEndpoint() {
        return new ApiManagementEndpoint();
    }

    @Bean
    public NodeApisEndpointInitializer nodeApisEndpointInitializer() {
        return new NodeApisEndpointInitializer();
    }

    @Bean
    public PolicyPluginFactory policyPluginFactory() {
        return new PolicyPluginFactoryImpl();
    }

    @Bean
    public io.gravitee.gateway.policy.PolicyFactoryCreator v3PolicyFactoryCreator(final PolicyPluginFactory policyPluginFactory) {
        return new PolicyFactoryCreatorImpl(configuration, policyPluginFactory, new ExpressionLanguageStringConditionEvaluator());
    }

    @Bean
    public PolicyFactory policyFactory(final PolicyPluginFactory policyPluginFactory) {
        return new DefaultPolicyFactory(
            policyPluginFactory,
            new ExpressionLanguageConditionFilter<>(),
            new ExpressionLanguageMessageConditionFilter<>()
        );
    }

    @Bean
    public ComponentProvider componentProvider() {
        return new SpringComponentProvider(applicationContext);
    }

    @Bean
    public DataEncryptor apiPropertiesEncryptor() {
        return new DataEncryptor(environment, "api.properties.encryption.secret", "vvLJ4Q8Khvv9tm2tIPdkGEdmgKUruAL6");
    }

    @Bean
    public ApiTemplateVariableProviderFactory apiTemplateVariableProviderFactory() {
        return new ApiTemplateVariableProviderFactory();
    }

    @Bean
    public ApiProcessorChainFactory apiProcessorChainFactory() {
        return new ApiProcessorChainFactory(configuration, node);
    }

    @Bean
    public io.gravitee.gateway.jupiter.handlers.api.flow.resolver.FlowResolverFactory flowResolverFactory() {
        return new io.gravitee.gateway.jupiter.handlers.api.flow.resolver.FlowResolverFactory(
            new CompositeConditionFilter<>(
                new HttpMethodConditionFilter(),
                new PathBasedConditionFilter(),
                new ExpressionLanguageConditionFilter<>()
            )
        );
    }

    @Bean
    public FlowResolverFactory v4FlowResolverFactory() {
        return new FlowResolverFactory(
            new CompositeConditionFilter<>(new HttpSelectorConditionFilter(), new ConditionSelectorConditionFilter()),
            new CompositeConditionFilter<>(new ChannelSelectorConditionFilter(), new ConditionSelectorConditionFilter())
        );
    }

    @Bean
    public ReactorFactory<Api> apiReactorFactory(
        io.gravitee.gateway.policy.PolicyFactoryCreator v3PolicyFactoryCreator,
        PolicyFactory policyFactory,
        @Qualifier("platformPolicyChainFactory") PolicyChainFactory platformPolicyChainFactory,
        OrganizationManager organizationManager,
        PolicyChainProviderLoader policyChainProviderLoader,
        ApiProcessorChainFactory apiProcessorChainFactory,
        io.gravitee.gateway.jupiter.handlers.api.flow.resolver.FlowResolverFactory flowResolverFactory,
        HttpRequestTimeoutConfiguration httpRequestTimeoutConfiguration
    ) {
        return new ApiReactorHandlerFactory(
            applicationContext,
            configuration,
            node,
            v3PolicyFactoryCreator,
            policyFactory,
            platformPolicyChainFactory,
            organizationManager,
            policyChainProviderLoader,
            apiProcessorChainFactory,
            flowResolverFactory,
            httpRequestTimeoutConfiguration
        );
    }

    @Bean
    public ApiKeyService apiKeyService(CacheManager cacheManager) {
        return new ApiKeyService(cacheManager);
    }

    @Bean
    public SubscriptionService subscriptionService(
        CacheManager cacheManager,
        ApiKeyService apiKeyService,
        SubscriptionDispatcher subscriptionDispatcher
    ) {
        return new SubscriptionService(cacheManager, apiKeyService, subscriptionDispatcher);
    }

    @Bean
    public io.gravitee.gateway.jupiter.handlers.api.v4.processor.ApiProcessorChainFactory v4ApiProcessorChainFactory() {
        return new io.gravitee.gateway.jupiter.handlers.api.v4.processor.ApiProcessorChainFactory(configuration, node);
    }

    @Bean
    public ApiMessageProcessorChainFactory apiMessageProcessorChainFactory() {
        return new ApiMessageProcessorChainFactory();
    }

    @Bean
    public ReactorFactory<io.gravitee.gateway.jupiter.handlers.api.v4.Api> asyncApiReactorFactory(
        PolicyFactory policyFactory,
        EntrypointConnectorPluginManager entrypointConnectorPluginManager,
        EndpointConnectorPluginManager endpointConnectorPluginManager,
        @Qualifier("platformPolicyChainFactory") PolicyChainFactory platformPolicyChainFactory,
        OrganizationManager organizationManager,
        io.gravitee.gateway.jupiter.handlers.api.v4.processor.ApiProcessorChainFactory v4ApiProcessorChainFactory,
        ApiMessageProcessorChainFactory apiMessageProcessorChainFactory,
        io.gravitee.gateway.jupiter.handlers.api.flow.resolver.FlowResolverFactory flowResolverFactory,
        FlowResolverFactory v4FlowResolverFactory
    ) {
        return new AsyncReactorFactory(
            applicationContext,
            configuration,
            node,
            policyFactory,
            entrypointConnectorPluginManager,
            endpointConnectorPluginManager,
            platformPolicyChainFactory,
            organizationManager,
            v4ApiProcessorChainFactory,
            apiMessageProcessorChainFactory,
            flowResolverFactory,
            v4FlowResolverFactory
        );
    }
}
