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
package io.gravitee.gateway.reactive.http.vertx;

import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.http.HttpVersion;
import io.gravitee.common.http.IdGenerator;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.common.util.URIUtils;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.http.vertx.VertxHttpHeaders;
import io.gravitee.gateway.reactive.api.context.Request;
import io.gravitee.reporter.api.http.Metrics;
import io.reactivex.*;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.net.SocketAddress;
import javax.net.ssl.SSLSession;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractVertxHttpServerRequest<T> implements Request<T> {

    protected final String id;
    protected final long timestamp;
    protected MultiValueMap<String, String> queryParameters = null;
    protected MultiValueMap<String, String> pathParameters = null;
    protected HttpHeaders headers;
    protected final Metrics metrics;
    protected final String contextPath;
    protected final String pathInfo;

    protected final HttpServerRequest nativeRequest;
    protected Flowable<T> content;

    public AbstractVertxHttpServerRequest(HttpServerRequest nativeRequest, String contextPath, IdGenerator idGenerator) {
        this.nativeRequest = nativeRequest;
        this.timestamp = System.currentTimeMillis();
        this.id = idGenerator.randomString();
        this.headers = new VertxHttpHeaders(nativeRequest.headers().getDelegate());
        this.metrics = Metrics.on(timestamp).build();
        this.metrics.setRequestId(id());
        this.metrics.setHttpMethod(method());
        this.metrics.setLocalAddress(localAddress());
        this.metrics.setRemoteAddress(remoteAddress());
        this.metrics.setHost(nativeRequest.host());
        this.metrics.setUri(uri());
        this.metrics.setUserAgent(nativeRequest.getHeader(io.vertx.reactivex.core.http.HttpHeaders.USER_AGENT));
        this.contextPath = contextPath;
        this.pathInfo = path().substring((contextPath.length() == 1) ? 0 : contextPath.length() - 1);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String transactionId() {
        throw new IllegalStateException("Request not yet managed.");
    }

    @Override
    public String uri() {
        return nativeRequest.uri();
    }

    @Override
    public String path() {
        return nativeRequest.path();
    }

    @Override
    public String pathInfo() {
        return pathInfo;
    }

    @Override
    public String contextPath() {
        return contextPath;
    }

    @Override
    public MultiValueMap<String, String> parameters() {
        if (queryParameters == null) {
            queryParameters = URIUtils.parameters(nativeRequest.uri());
        }

        return queryParameters;
    }

    @Override
    public MultiValueMap<String, String> pathParameters() {
        if (pathParameters == null) {
            pathParameters = new LinkedMultiValueMap<>();
        }

        return pathParameters;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpMethod method() {
        try {
            return HttpMethod.valueOf(nativeRequest.method().name());
        } catch (IllegalArgumentException iae) {
            return HttpMethod.OTHER;
        }
    }

    @Override
    public String scheme() {
        return nativeRequest.scheme();
    }

    @Override
    public HttpVersion version() {
        return HttpVersion.valueOf(nativeRequest.version().name());
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public String remoteAddress() {
        SocketAddress address = nativeRequest.remoteAddress();
        if (address == null) {
            return null;
        }

        //TODO: To be removed
        int ipv6Idx = address.host().indexOf("%");

        return (ipv6Idx != -1) ? address.host().substring(0, ipv6Idx) : address.host();
    }

    @Override
    public String localAddress() {
        SocketAddress address = nativeRequest.localAddress();
        if (address == null) {
            return null;
        }

        //TODO: To be removed
        int ipv6Idx = address.host().indexOf("%");

        return (ipv6Idx != -1) ? address.host().substring(0, ipv6Idx) : address.host();
    }

    @Override
    public SSLSession sslSession() {
        return nativeRequest.sslSession();
    }

    @Override
    public Metrics metrics() {
        return metrics;
    }

    @Override
    public boolean ended() {
        return nativeRequest.isEnded();
    }

    @Override
    public String host() {
        return this.nativeRequest.host();
    }
}