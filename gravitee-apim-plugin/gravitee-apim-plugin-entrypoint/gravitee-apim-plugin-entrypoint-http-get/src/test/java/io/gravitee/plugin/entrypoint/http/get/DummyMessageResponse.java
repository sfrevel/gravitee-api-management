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
package io.gravitee.plugin.entrypoint.http.get;

import io.gravitee.common.http.HttpMethod;
import io.gravitee.common.http.HttpVersion;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.gateway.jupiter.api.context.HttpResponse;
import io.gravitee.gateway.jupiter.api.context.MessageRequest;
import io.gravitee.gateway.jupiter.api.context.MessageResponse;
import io.gravitee.gateway.jupiter.api.message.Message;
import io.gravitee.reporter.api.http.Metrics;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import javax.net.ssl.SSLSession;

public class DummyMessageResponse implements MessageResponse {

    Flowable<Message> messages;

    Buffer buffer;

    HttpHeaders headers = HttpHeaders.create();

    @Override
    public Flowable<Message> messages() {
        return messages;
    }

    @Override
    public void messages(Flowable<Message> messages) {
        this.messages = messages;
    }

    @Override
    public Completable end(Buffer buffer) {
        this.buffer = buffer;
        return Completable.complete();
    }

    public Buffer getBuffer() {
        return buffer;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public HttpResponse status(int statusCode) {
        return null;
    }

    @Override
    public int status() {
        return 0;
    }

    @Override
    public String reason() {
        return null;
    }

    @Override
    public HttpResponse reason(String message) {
        return null;
    }

    @Override
    public HttpHeaders trailers() {
        return null;
    }

    @Override
    public boolean ended() {
        return false;
    }

    @Override
    public Completable onMessages(FlowableTransformer<Message, Message> onMessages) {
        return null;
    }

    @Override
    public Completable end() {
        return Completable.complete();
    }

    @Override
    public Completable write(Buffer buffer) {
        return null;
    }

    @Override
    public Completable writeHeaders() {
        return null;
    }
}
