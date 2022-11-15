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

import static io.gravitee.gateway.core.logging.utils.LoggingUtils.isContentTypeLoggable;

import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaderNames;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.core.logging.LoggingContext;
import io.gravitee.gateway.core.logging.utils.LoggingUtils;
import io.gravitee.gateway.jupiter.api.context.GenericRequest;
import io.gravitee.gateway.jupiter.api.context.HttpRequest;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
abstract class LogRequest extends io.gravitee.reporter.api.common.Request {

    protected final LoggingContext loggingContext;
    protected final GenericRequest request;

    public LogRequest(LoggingContext loggingContext, HttpRequest request) {
        this.loggingContext = loggingContext;
        this.request = request;

        this.setMethod(request.method());

        if (isLogPayload() && isContentTypeLoggable(request.headers().get(HttpHeaderNames.CONTENT_TYPE), loggingContext)) {
            final Buffer buffer = Buffer.buffer();

            request.chunks(
                request
                    .chunks()
                    .doOnNext(chunk -> LoggingUtils.appendBuffer(buffer, chunk, loggingContext.getMaxSizeLogMessage()))
                    .doOnComplete(() -> this.setBody(buffer.toString()))
            );
        }

        if (isLogHeaders()) {
            this.setHeaders(HttpHeaders.create(request.headers()));
        }
    }

    protected abstract boolean isLogPayload();

    protected abstract boolean isLogHeaders();
}
