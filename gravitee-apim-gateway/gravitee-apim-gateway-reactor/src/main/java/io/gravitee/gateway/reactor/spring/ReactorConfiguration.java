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
package io.gravitee.gateway.reactor.spring;

import static io.gravitee.gateway.jupiter.reactor.processor.transaction.TransactionProcessorFactory.DEFAULT_REQUEST_ID_HEADER;
import static io.gravitee.gateway.jupiter.reactor.processor.transaction.TransactionProcessorFactory.DEFAULT_TRANSACTION_ID_HEADER;

import io.gravitee.common.event.EventManager;
import io.gravitee.common.http.IdGenerator;
import io.gravitee.common.utils.Hex;
import io.gravitee.common.utils.UUID;
import io.gravitee.gateway.core.component.ComponentProvider;
import io.gravitee.gateway.env.GatewayConfiguration;
import io.gravitee.gateway.env.HttpRequestTimeoutConfiguration;
import io.gravitee.gateway.jupiter.reactor.DefaultHttpRequestDispatcher;
import io.gravitee.gateway.jupiter.reactor.HttpRequestDispatcher;
import io.gravitee.gateway.jupiter.reactor.handler.DefaultHttpAcceptorResolver;
import io.gravitee.gateway.jupiter.reactor.handler.HttpAcceptorResolver;
import io.gravitee.gateway.jupiter.reactor.processor.NotFoundProcessorChainFactory;
import io.gravitee.gateway.jupiter.reactor.processor.PlatformProcessorChainFactory;
import io.gravitee.gateway.jupiter.reactor.v4.reactor.ReactorFactory;
import io.gravitee.gateway.jupiter.reactor.v4.reactor.ReactorFactoryManager;
import io.gravitee.gateway.jupiter.reactor.v4.subscription.*;
import io.gravitee.gateway.reactor.Reactor;
import io.gravitee.gateway.reactor.handler.AcceptorResolver;
import io.gravitee.gateway.reactor.handler.ReactorEventListener;
import io.gravitee.gateway.reactor.handler.ReactorHandlerRegistry;
import io.gravitee.gateway.reactor.handler.context.provider.NodeTemplateVariableProvider;
import io.gravitee.gateway.reactor.handler.impl.DefaultAcceptorResolver;
import io.gravitee.gateway.reactor.handler.impl.DefaultReactorHandlerRegistry;
import io.gravitee.gateway.reactor.impl.DefaultReactor;
import io.gravitee.gateway.reactor.processor.RequestProcessorChainFactory;
import io.gravitee.gateway.reactor.processor.ResponseProcessorChainFactory;
import io.gravitee.gateway.reactor.processor.transaction.TraceContextProcessorFactory;
import io.gravitee.gateway.reactor.processor.transaction.TransactionProcessorFactory;
import io.gravitee.gateway.report.ReporterService;
import io.gravitee.node.api.Node;
import io.gravitee.plugin.alert.AlertEventProducer;
import io.vertx.core.Vertx;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class ReactorConfiguration {

    public static final Logger log = LoggerFactory.getLogger(ReactorConfiguration.class);

    private static final String HEX_FORMAT = "hex";

    @Bean
    public Reactor v3Reactor(
        final @Qualifier("v3AcceptorResolver") AcceptorResolver acceptorResolver,
        final GatewayConfiguration gatewayConfiguration,
        final @Qualifier("v3RequestProcessorChainFactory") RequestProcessorChainFactory requestProcessorChainFactory,
        final @Qualifier("v3ResponseProcessorChainFactory") ResponseProcessorChainFactory responseProcessorChainFactory,
        final @Qualifier(
            "v3NotFoundProcessorChainFactory"
        ) io.gravitee.gateway.reactor.processor.NotFoundProcessorChainFactory notFoundProcessorChainFactory
    ) {
        // DefaultReactor bean must be kept while we are still supporting v3 execution mode.
        return new DefaultReactor(
            acceptorResolver,
            gatewayConfiguration,
            requestProcessorChainFactory,
            responseProcessorChainFactory,
            notFoundProcessorChainFactory
        );
    }

    @Bean
    public AcceptorResolver v3AcceptorResolver(ReactorHandlerRegistry reactorHandlerRegistry) {
        // V3 EntrypointResolver bean must be kept while we are still supporting v3 execution mode.
        return new DefaultAcceptorResolver(reactorHandlerRegistry);
    }

    @Bean
    public IdGenerator idGenerator(@Value("${handlers.request.format:uuid}") String requestFormat) {
        if (HEX_FORMAT.equals(requestFormat)) {
            return new Hex();
        } else {
            return new UUID();
        }
    }

    @Bean
    public io.gravitee.gateway.jupiter.reactor.processor.transaction.TransactionProcessorFactory transactionHandlerFactory(
        @Value("${handlers.request.transaction.header:" + DEFAULT_TRANSACTION_ID_HEADER + "}") String transactionHeader,
        @Value("${handlers.request.request.header:" + DEFAULT_REQUEST_ID_HEADER + "}") String requestHeader
    ) {
        return new io.gravitee.gateway.jupiter.reactor.processor.transaction.TransactionProcessorFactory(transactionHeader, requestHeader);
    }

    @Bean
    public PlatformProcessorChainFactory globalProcessorChainFactory(
        io.gravitee.gateway.jupiter.reactor.processor.transaction.TransactionProcessorFactory transactionHandlerFactory,
        @Value("${handlers.request.trace-context.enabled:false}") boolean traceContext,
        ReporterService reporterService,
        AlertEventProducer eventProducer,
        Node node,
        @Value("${http.port:8082}") String httpPort,
        @Value("${services.tracing.enabled:false}") boolean tracing
    ) {
        return new PlatformProcessorChainFactory(
            transactionHandlerFactory,
            traceContext,
            reporterService,
            eventProducer,
            node,
            httpPort,
            tracing
        );
    }

    @Bean
    public HttpRequestDispatcher httpRequestDispatcher(
        GatewayConfiguration gatewayConfiguration,
        HttpAcceptorResolver httpAcceptorResolver,
        IdGenerator idGenerator,
        ComponentProvider globalComponentProvider,
        RequestProcessorChainFactory v3RequestProcessorChainFactory,
        ResponseProcessorChainFactory v3ResponseProcessorChainFactory,
        PlatformProcessorChainFactory platformProcessorChainFactory,
        NotFoundProcessorChainFactory notFoundProcessorChainFactory,
        @Value("${services.tracing.enabled:false}") boolean tracingEnabled,
        HttpRequestTimeoutConfiguration httpRequestTimeoutConfiguration,
        Vertx vertx
    ) {
        return new DefaultHttpRequestDispatcher(
            gatewayConfiguration,
            httpAcceptorResolver,
            idGenerator,
            globalComponentProvider,
            v3RequestProcessorChainFactory,
            v3ResponseProcessorChainFactory,
            platformProcessorChainFactory,
            notFoundProcessorChainFactory,
            tracingEnabled,
            httpRequestTimeoutConfiguration,
            vertx
        );
    }

    @Bean
    public SubscriptionAcceptorResolver subscriptionAcceptorResolver(ReactorHandlerRegistry reactorHandlerRegistry) {
        return new DefaultSubscriptionAcceptorResolver(reactorHandlerRegistry);
    }

    @Bean
    public SubscriptionExecutionRequestFactory subscriptionExecutionRequestFactory() {
        return new SubscriptionExecutionRequestFactory();
    }

    @Bean
    public SubscriptionDispatcher subscriptionDispatcher(
        SubscriptionAcceptorResolver subscriptionAcceptorResolver,
        SubscriptionExecutionRequestFactory subscriptionExecutionRequestFactory
    ) {
        return new DefaultSubscriptionDispatcher(subscriptionAcceptorResolver, subscriptionExecutionRequestFactory);
    }

    @Bean
    public HttpAcceptorResolver httpAcceptorResolver(ReactorHandlerRegistry reactorHandlerRegistry) {
        return new DefaultHttpAcceptorResolver(reactorHandlerRegistry);
    }

    @Bean
    public ReactorEventListener reactorEventListener(EventManager eventManager, ReactorHandlerRegistry reactorHandlerRegistry) {
        return new ReactorEventListener(eventManager, reactorHandlerRegistry);
    }

    @Bean
    public ReactorHandlerRegistry reactorHandlerRegistry(ReactorFactoryManager reactorFactoryManager) {
        return new DefaultReactorHandlerRegistry(reactorFactoryManager);
    }

    @Bean
    public ReactorFactoryManager reactorFactoryManager(List<ReactorFactory> reactorFactories) {
        return new ReactorFactoryManager(reactorFactories);
    }

    @Bean
    public TransactionProcessorFactory v3TransactionHandlerFactory() {
        return new io.gravitee.gateway.reactor.processor.transaction.TransactionProcessorFactory();
    }

    @Bean
    public TraceContextProcessorFactory v3TraceContextProcessorFactory() {
        return new TraceContextProcessorFactory();
    }

    @Bean
    public io.gravitee.gateway.reactor.processor.RequestProcessorChainFactory v3RequestProcessorChainFactory() {
        return new io.gravitee.gateway.reactor.processor.RequestProcessorChainFactory();
    }

    @Bean
    public io.gravitee.gateway.reactor.processor.ResponseProcessorChainFactory v3ResponseProcessorChainFactory() {
        return new io.gravitee.gateway.reactor.processor.ResponseProcessorChainFactory();
    }

    @Bean
    public io.gravitee.gateway.reactor.processor.NotFoundProcessorChainFactory v3NotFoundProcessorChainFactory() {
        return new io.gravitee.gateway.reactor.processor.NotFoundProcessorChainFactory();
    }

    @Bean
    public NotFoundProcessorChainFactory notFoundProcessorChainFactory(
        Environment environment,
        ReporterService reporterService,
        @Value("${handlers.notfound.log.enabled:false}") boolean logEnabled,
        @Value("${services.tracing.enabled:false}") boolean tracingEnabled
    ) {
        return new NotFoundProcessorChainFactory(environment, reporterService, logEnabled, tracingEnabled);
    }

    @Bean
    public NodeTemplateVariableProvider nodeTemplateVariableProvider(Node node, GatewayConfiguration gatewayConfiguration) {
        return new NodeTemplateVariableProvider(node, gatewayConfiguration);
    }
}
