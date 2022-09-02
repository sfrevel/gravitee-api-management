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
package io.gravitee.gateway.jupiter.policy;

import io.gravitee.definition.model.ConditionSupplier;
import io.gravitee.gateway.jupiter.api.context.GenericExecutionContext;
import io.gravitee.gateway.jupiter.api.context.HttpExecutionContext;
import io.gravitee.gateway.jupiter.api.context.MessageExecutionContext;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.gravitee.gateway.jupiter.api.policy.Policy;
import io.gravitee.gateway.jupiter.core.condition.ConditionFilter;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author GraviteeSource Team
 */
public class ConditionalPolicy implements Policy, ConditionSupplier {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConditionalPolicy.class);

    protected final Policy policy;
    protected final String condition;
    private final ConditionFilter<ConditionalPolicy> conditionFilter;

    public ConditionalPolicy(Policy policy, String condition, ConditionFilter<ConditionalPolicy> conditionFilter) {
        this.policy = policy;
        this.condition = condition;
        this.conditionFilter = conditionFilter;
    }

    @Override
    public String id() {
        return policy.id();
    }

    @Override
    public Completable onRequest(HttpExecutionContext ctx) {
        return onCondition(ctx, policy.onRequest(ctx));
    }

    @Override
    public Completable onResponse(HttpExecutionContext ctx) {
        return onCondition(ctx, policy.onResponse(ctx));
    }

    @Override
    public Completable onMessageRequest(final MessageExecutionContext ctx) {
        return onCondition(ctx, policy.onMessageRequest(ctx));
    }

    @Override
    public Completable onMessageResponse(final MessageExecutionContext ctx) {
        return onCondition(ctx, policy.onMessageResponse(ctx));
    }

    @Override
    public String getCondition() {
        return condition;
    }

    private Completable onCondition(GenericExecutionContext ctx, Completable toExecute) {
        return conditionFilter.filter(ctx, this).flatMapCompletable(conditionalPolicy -> toExecute);
    }

    private Maybe<Message> onCondition(GenericExecutionContext ctx, Maybe<Message> toExecute) {
        return conditionFilter.filter(ctx, this).flatMap(conditionalPolicy -> toExecute);
    }

    private Flowable<Message> onCondition(GenericExecutionContext ctx, Flowable<Message> toExecute) {
        return conditionFilter.filter(ctx, this).flatMapPublisher(conditionalPolicy -> toExecute);
    }
}
