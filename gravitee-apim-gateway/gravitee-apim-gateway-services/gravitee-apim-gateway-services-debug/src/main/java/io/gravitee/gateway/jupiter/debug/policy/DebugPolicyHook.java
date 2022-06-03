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
package io.gravitee.gateway.jupiter.debug.policy;

import io.gravitee.gateway.jupiter.api.ExecutionFailure;
import io.gravitee.gateway.jupiter.api.ExecutionPhase;
import io.gravitee.gateway.jupiter.api.context.RequestExecutionContext;
import io.gravitee.gateway.jupiter.api.hook.PolicyHook;
import io.gravitee.gateway.jupiter.debug.reactor.context.DebugRequestExecutionContext;
import io.reactivex.Completable;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DebugPolicyHook implements PolicyHook {

    @Override
    public String id() {
        return "hook-debug-policy";
    }

    @Override
    public Completable pre(final String id, final RequestExecutionContext ctx, final ExecutionPhase executionPhase) {
        DebugRequestExecutionContext debugCtx = getExecutionContext(ctx);
        if (debugCtx != null) {
            return debugCtx.prePolicyExecution(id, executionPhase);
        }
        return Completable.error(new IllegalArgumentException("Given context is not a DebugRequestExecutionContext"));
    }

    @Override
    public Completable post(final String id, final RequestExecutionContext ctx, final ExecutionPhase executionPhase) {
        DebugRequestExecutionContext debugCtx = getExecutionContext(ctx);
        if (debugCtx != null) {
            return debugCtx.postPolicyExecution();
        }
        return Completable.error(new IllegalArgumentException("Given context is not a DebugRequestExecutionContext"));
    }

    @Override
    public Completable error(
        final String id,
        final RequestExecutionContext ctx,
        final ExecutionPhase executionPhase,
        final Throwable throwable
    ) {
        DebugRequestExecutionContext debugCtx = getExecutionContext(ctx);
        if (debugCtx != null) {
            return debugCtx.postPolicyExecution(throwable);
        }
        return Completable.error(new IllegalArgumentException("Given context is not a DebugRequestExecutionContext"));
    }

    @Override
    public Completable interrupt(final String id, final RequestExecutionContext ctx, final ExecutionPhase executionPhase) {
        DebugRequestExecutionContext debugCtx = getExecutionContext(ctx);
        if (debugCtx != null) {
            return debugCtx.postPolicyExecution();
        }
        return Completable.error(new IllegalArgumentException("Given context is not a DebugRequestExecutionContext"));
    }

    @Override
    public Completable interruptWith(
        final String id,
        final RequestExecutionContext ctx,
        final ExecutionPhase executionPhase,
        final ExecutionFailure failure
    ) {
        return Completable.defer(() -> {
            DebugRequestExecutionContext debugCtx = getExecutionContext(ctx);
            if (debugCtx != null) {
                return debugCtx.postPolicyExecution(failure);
            }
            return Completable.error(new IllegalArgumentException("Given context is not a DebugRequestExecutionContext"));
        });
    }

    private DebugRequestExecutionContext getExecutionContext(final RequestExecutionContext ctx) {
        if (ctx instanceof DebugRequestExecutionContext) {
            return (DebugRequestExecutionContext) ctx;
        }
        return null;
    }
}
