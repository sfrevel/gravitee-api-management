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
package io.gravitee.rest.api.service.v4.exception;

import static java.util.Collections.singletonMap;

import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.rest.api.service.exceptions.AbstractManagementException;
import java.util.Map;
import java.util.Set;

/**
 * @author Guillaume LAMIRAND (guillaume.lamirand at graviteesource.com)
 * @author GraviteeSource Team
 */
public class FlowSelectorsEntrypointInvalidException extends AbstractManagementException {

    private final String name;
    private final Set<String> invalidEntrypoints;

    public FlowSelectorsEntrypointInvalidException(final String name, final Set<String> invalidEntrypoints) {
        this.name = name;
        this.invalidEntrypoints = invalidEntrypoints;
    }

    @Override
    public int getHttpStatusCode() {
        return HttpStatusCode.BAD_REQUEST_400;
    }

    @Override
    public String getMessage() {
        return "The flow [" + name + "] contains channel selector with invalid entrypoints " + invalidEntrypoints + ".";
    }

    @Override
    public String getTechnicalCode() {
        return "flow.selectors.entrypoints.invalid";
    }

    @Override
    public Map<String, String> getParameters() {
        return singletonMap("flow.selectors.entrypoints.invalid", invalidEntrypoints.toString());
    }
}
