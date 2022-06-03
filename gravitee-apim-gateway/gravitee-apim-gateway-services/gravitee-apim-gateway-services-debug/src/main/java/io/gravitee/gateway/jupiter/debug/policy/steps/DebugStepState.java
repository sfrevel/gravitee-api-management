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
package io.gravitee.gateway.jupiter.debug.policy.steps;

import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaders;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DebugStepState {

    private HttpHeaders headers;
    private Map<String, Serializable> attributes;
    private MultiValueMap<String, String> parameters;
    private MultiValueMap<String, String> pathParameters;
    private String path;
    private String contextPath;
    private HttpMethod method;
    private Integer statusCode;
    private String reason;
    private Buffer buffer;

    public HttpHeaders headers() {
        return headers;
    }

    public DebugStepState headers(HttpHeaders headers) {
        this.headers = HttpHeaders.create(headers);
        return this;
    }

    public Map<String, Serializable> attributes() {
        return attributes;
    }

    public DebugStepState attributes(Map<String, Serializable> attributes) {
        this.attributes = new HashMap<>(attributes);
        return this;
    }

    public MultiValueMap<String, String> parameters() {
        return parameters;
    }

    public DebugStepState parameters(MultiValueMap<String, String> parameters) {
        this.parameters = new LinkedMultiValueMap<>(parameters);
        return this;
    }

    public MultiValueMap<String, String> pathParameters() {
        return pathParameters;
    }

    public DebugStepState pathParameters(MultiValueMap<String, String> pathParameters) {
        this.pathParameters = new LinkedMultiValueMap<>(pathParameters);
        return this;
    }

    public String path() {
        return path;
    }

    public DebugStepState path(String path) {
        this.path = path;
        return this;
    }

    public String contextPath() {
        return contextPath;
    }

    public DebugStepState contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public HttpMethod method() {
        return method;
    }

    public DebugStepState method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public DebugStepState statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String reason() {
        return reason;
    }

    public DebugStepState reason(String reason) {
        this.reason = reason;
        return this;
    }

    public Buffer buffer() {
        return buffer;
    }

    public DebugStepState buffer(final Buffer buffer) {
        this.buffer = buffer;
        return this;
    }
}
