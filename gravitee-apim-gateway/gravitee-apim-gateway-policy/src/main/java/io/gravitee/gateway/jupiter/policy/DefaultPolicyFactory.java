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

import io.gravitee.gateway.jupiter.api.ExecutionPhase;
import io.gravitee.gateway.jupiter.api.policy.Policy;
import io.gravitee.gateway.jupiter.core.condition.ExpressionLanguageConditionFilter;
import io.gravitee.gateway.jupiter.core.condition.ExpressionLanguageMessageConditionFilter;
import io.gravitee.gateway.jupiter.policy.adapter.policy.PolicyAdapter;
import io.gravitee.gateway.policy.PolicyManifest;
import io.gravitee.gateway.policy.PolicyMetadata;
import io.gravitee.gateway.policy.PolicyPluginFactory;
import io.gravitee.gateway.policy.StreamType;
import io.gravitee.policy.api.PolicyConfiguration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Guillaume Lamirand (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DefaultPolicyFactory implements PolicyFactory {

    private final ConcurrentMap<String, Policy> policies = new ConcurrentHashMap<>();
    private final PolicyPluginFactory policyPluginFactory;
    private final io.gravitee.gateway.policy.PolicyFactory v3PolicyFactory;
    private final ExpressionLanguageConditionFilter<ConditionalPolicy> filter;
    private final ExpressionLanguageMessageConditionFilter<ConditionalPolicy> messageFilter;

    public DefaultPolicyFactory(
        final PolicyPluginFactory policyPluginFactory,
        final ExpressionLanguageConditionFilter<ConditionalPolicy> filter,
        final ExpressionLanguageMessageConditionFilter<ConditionalPolicy> messageFilter
    ) {
        this.policyPluginFactory = policyPluginFactory;
        this.filter = filter;
        this.messageFilter = messageFilter;
        // V3 policy factory doesn't need condition evaluator anymore as condition is directly handled by jupiter.
        this.v3PolicyFactory = new io.gravitee.gateway.policy.impl.PolicyFactoryImpl(policyPluginFactory);
    }

    @Override
    public Policy create(
        final ExecutionPhase executionPhase,
        final PolicyManifest policyManifest,
        final PolicyConfiguration policyConfiguration,
        final PolicyMetadata policyMetadata
    ) {
        return policies.computeIfAbsent(
            generateKey(
                executionPhase,
                policyManifest,
                policyConfiguration,
                policyMetadata.getCondition(),
                policyMetadata.getMessageCondition()
            ),
            k -> createPolicy(executionPhase, policyManifest, policyConfiguration, policyMetadata)
        );
    }

    private Policy createPolicy(
        final ExecutionPhase phase,
        final PolicyManifest policyManifest,
        final PolicyConfiguration policyConfiguration,
        final PolicyMetadata policyMetadata
    ) {
        Policy policy = null;

        if (Policy.class.isAssignableFrom(policyManifest.policy())) {
            policy = (Policy) policyPluginFactory.create(policyManifest.policy(), policyConfiguration);
        } else if (phase == ExecutionPhase.REQUEST || phase == ExecutionPhase.RESPONSE) {
            StreamType streamType = phase == ExecutionPhase.REQUEST ? StreamType.ON_REQUEST : StreamType.ON_RESPONSE;
            if (policyManifest.accept(streamType)) {
                io.gravitee.gateway.policy.Policy v3Policy = v3PolicyFactory.create(
                    streamType,
                    policyManifest,
                    policyConfiguration,
                    policyMetadata
                );
                policy = new PolicyAdapter(v3Policy);
            }
        } else {
            throw new IllegalArgumentException(
                String.format("Cannot create policy instance with [phase=%s, policy=%s]", phase, policyManifest.id())
            );
        }

        if (policy != null) {
            final String condition = policyMetadata.getCondition();
            final String messageCondition = policyMetadata.getMessageCondition();

            // Avoid creating a conditional policy if no condition or message condition is defined.
            if (isNotBlank(condition) || isNotBlank(messageCondition)) {
                policy = new ConditionalPolicy(policy, condition, messageCondition, filter, messageFilter);
            }
        }

        return policy;
    }

    @Override
    public void cleanup(PolicyManifest policyManifest) {
        policyPluginFactory.cleanup(policyManifest);
    }

    private String generateKey(
        final ExecutionPhase executionPhase,
        final PolicyManifest policyManifest,
        final PolicyConfiguration policyConfiguration,
        final String condition,
        final String messageCondition
    ) {
        return (
            Objects.hashCode(executionPhase) +
            "-" +
            Objects.hashCode(policyManifest) +
            "-" +
            Objects.hashCode(policyConfiguration) +
            "-" +
            Objects.hashCode(condition) +
            "-" +
            Objects.hashCode(messageCondition)
        );
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
