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
package io.gravitee.gateway.jupiter.debug.reactor.context;

import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.debug.core.invoker.InvokerResponse;
import io.gravitee.gateway.debug.reactor.handler.context.AttributeHelper;
import io.gravitee.gateway.jupiter.api.ExecutionFailure;
import io.gravitee.gateway.jupiter.api.ExecutionPhase;
import io.gravitee.gateway.jupiter.core.context.MutableRequest;
import io.gravitee.gateway.jupiter.core.context.MutableResponse;
import io.gravitee.gateway.jupiter.debug.policy.steps.DebugRequestStep;
import io.gravitee.gateway.jupiter.debug.policy.steps.DebugResponseStep;
import io.gravitee.gateway.jupiter.debug.policy.steps.DebugStep;
import io.gravitee.gateway.jupiter.debug.policy.steps.DebugStepFactory;
import io.gravitee.gateway.jupiter.reactor.handler.context.DefaultRequestExecutionContext;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DebugRequestExecutionContext extends DefaultRequestExecutionContext {

    private final LinkedList<DebugStep<?>> debugSteps = new LinkedList<>();
    private final Map<String, Serializable> initialAttributes;
    private final InvokerResponse invokerResponse = new InvokerResponse();
    private final HttpHeaders initialHeaders;

    public DebugRequestExecutionContext(final MutableRequest request, final MutableResponse response) {
        super(request, response);
        this.initialAttributes = AttributeHelper.filterAndSerializeAttributes(getAttributes());
        this.initialHeaders = HttpHeaders.create(request().headers());
    }

    public Completable prePolicyExecution(final String id, final ExecutionPhase executionPhase) {
        return Maybe
            .fromCallable(() -> {
                String flowStage = getInternalAttribute(ATTR_INTERNAL_FLOW_STAGE);
                DebugStep<?> debugStep = DebugStepFactory.createDebugStep(id, executionPhase, flowStage);
                if (debugStep != null) {
                    if (!debugSteps.contains(debugStep)) {
                        debugSteps.add(debugStep);
                        return debugStep;
                    }
                }
                return null;
            })
            .flatMapCompletable(debugStep -> {
                if (ExecutionPhase.REQUEST == debugStep.getExecutionPhase()) {
                    return ((DebugRequestStep) debugStep).pre(request(), getAttributes());
                } else if (ExecutionPhase.RESPONSE == debugStep.getExecutionPhase()) {
                    return ((DebugResponseStep) debugStep).pre(response(), getAttributes());
                }
                return Completable.complete();
            });
    }

    public Completable postPolicyExecution() {
        return Maybe
            .fromCallable(() -> {
                DebugStep<?> currentDebugStep = getCurrentDebugStep();
                if (currentDebugStep != null) {
                    if (!currentDebugStep.isEnded()) {
                        return currentDebugStep;
                    }
                }
                return null;
            })
            .flatMapCompletable(currentDebugStep -> {
                if (ExecutionPhase.REQUEST == currentDebugStep.getExecutionPhase()) {
                    return ((DebugRequestStep) currentDebugStep).post(request(), getAttributes());
                } else if (ExecutionPhase.RESPONSE == currentDebugStep.getExecutionPhase()) {
                    return ((DebugResponseStep) currentDebugStep).post(response(), getAttributes());
                }
                return Completable.complete();
            });
    }

    public Completable postPolicyExecution(final Throwable throwable) {
        return Completable
            .defer(() -> {
                DebugStep<?> currentDebugStep = getCurrentDebugStep();
                if (currentDebugStep != null) {
                    return currentDebugStep.error(throwable);
                }
                return Completable.complete();
            })
            .doOnComplete(() -> {
                DebugStep<?> currentDebugStep = getCurrentDebugStep();
                currentDebugStep.ended();
            });
    }

    public Completable postPolicyExecution(final ExecutionFailure executionFailure) {
        return Completable
            .defer(() -> {
                DebugStep<?> currentDebugStep = getCurrentDebugStep();
                if (currentDebugStep != null) {
                    return currentDebugStep.error(executionFailure);
                }
                return Completable.complete();
            })
            .doOnComplete(() -> {
                DebugStep<?> currentDebugStep = getCurrentDebugStep();
                currentDebugStep.ended();
            });
    }

    public Map<String, Serializable> getInitialAttributes() {
        return initialAttributes;
    }

    public HttpHeaders getInitialHeaders() {
        return initialHeaders;
    }

    private DebugStep<?> getCurrentDebugStep() {
        return debugSteps.getLast();
    }

    public List<DebugStep<?>> getDebugSteps() {
        return this.debugSteps;
    }

    public InvokerResponse getInvokerResponse() {
        return invokerResponse;
    }
}
