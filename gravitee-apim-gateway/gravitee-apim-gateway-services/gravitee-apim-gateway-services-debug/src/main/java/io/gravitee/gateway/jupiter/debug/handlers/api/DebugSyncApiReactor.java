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
package io.gravitee.gateway.jupiter.debug.handlers.api;

import io.gravitee.el.TemplateVariableProvider;
import io.gravitee.gateway.core.component.ComponentProvider;
import io.gravitee.gateway.core.endpoint.lifecycle.GroupLifecycleManager;
import io.gravitee.gateway.debug.definition.DebugApi;
import io.gravitee.gateway.debug.reactor.handler.context.PathTransformer;
import io.gravitee.gateway.handlers.api.definition.Api;
import io.gravitee.gateway.jupiter.api.invoker.Invoker;
import io.gravitee.gateway.jupiter.core.context.MutableRequestExecutionContext;
import io.gravitee.gateway.jupiter.handlers.api.SyncApiReactor;
import io.gravitee.gateway.jupiter.handlers.api.flow.FlowChainFactory;
import io.gravitee.gateway.jupiter.handlers.api.processor.ApiProcessorChainFactory;
import io.gravitee.gateway.jupiter.policy.PolicyManager;
import io.gravitee.gateway.resource.ResourceLifecycleManager;
import io.gravitee.node.api.configuration.Configuration;
import io.reactivex.Completable;
import java.util.List;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DebugSyncApiReactor extends SyncApiReactor {

    public DebugSyncApiReactor(
        final Api api,
        final ComponentProvider componentProvider,
        final List<TemplateVariableProvider> templateVariableProviders,
        final Invoker defaultInvoker,
        final ResourceLifecycleManager resourceLifecycleManager,
        final ApiProcessorChainFactory apiProcessorChainFactory,
        final PolicyManager policyManager,
        final FlowChainFactory flowChainFactory,
        final GroupLifecycleManager groupLifecycleManager,
        final Configuration configuration
    ) {
        super(
            api,
            componentProvider,
            templateVariableProviders,
            defaultInvoker,
            resourceLifecycleManager,
            apiProcessorChainFactory,
            policyManager,
            flowChainFactory,
            groupLifecycleManager,
            configuration
        );
    }

    @Override
    public Completable handle(final MutableRequestExecutionContext ctx) {
        String debugContextPath = ctx.request().contextPath();
        ctx.request().contextPath(PathTransformer.removeEventIdFromPath(((DebugApi) api).getEventId(), debugContextPath));
        String debugPath = ctx.request().path();
        ctx.request().pathInfo(PathTransformer.removeEventIdFromPath(((DebugApi) api).getEventId(), debugPath));
        return super.handle(ctx);
    }
}
