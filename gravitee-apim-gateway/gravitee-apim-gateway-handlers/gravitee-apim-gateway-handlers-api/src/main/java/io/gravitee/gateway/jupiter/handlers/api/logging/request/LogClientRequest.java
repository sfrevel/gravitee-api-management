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
package io.gravitee.gateway.jupiter.handlers.api.logging.request;

import io.gravitee.gateway.core.logging.LoggingContext;
import io.gravitee.gateway.jupiter.api.context.HttpRequest;

/**
 * Allows to log the response status, headers and body sent by the client depending on what is configured on the {@link LoggingContext}.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class LogClientRequest extends LogRequest {

    public LogClientRequest(LoggingContext loggingContext, HttpRequest request) {
        super(loggingContext, request);
        this.setUri(request.uri());
    }

    protected boolean isLogPayload() {
        return loggingContext.requestPayload();
    }

    protected boolean isLogHeaders() {
        return loggingContext.requestHeaders();
    }
}
