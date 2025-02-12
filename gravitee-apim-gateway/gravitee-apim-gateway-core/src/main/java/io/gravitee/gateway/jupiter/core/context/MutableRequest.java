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
package io.gravitee.gateway.jupiter.core.context;

import io.gravitee.gateway.jupiter.api.context.Request;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import java.util.function.Function;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface MutableRequest extends Request, OnMessagesInterceptor {
    /**
     * Allow setting context path.
     *
     * @return {@link MutableRequest}.
     */
    MutableRequest contextPath(final String contextPath);

    /**
     * Allow setting path info.
     *
     * @return {@link MutableRequest}.
     */
    MutableRequest pathInfo(final String pathInfo);

    /**
     * Allow setting transaction id.
     *
     * @return {@link MutableRequest}.
     */
    MutableRequest transactionId(final String id);

    /**
     * Allow setting client identifier.
     *
     * @return {@link MutableRequest}.
     */
    MutableRequest clientIdentifier(final String id);

    /**
     * Allow overriding remote adresse
     *
     * @return {@link MutableRequest}.
     */
    MutableRequest remoteAddress(final String remoteAddress);
}
