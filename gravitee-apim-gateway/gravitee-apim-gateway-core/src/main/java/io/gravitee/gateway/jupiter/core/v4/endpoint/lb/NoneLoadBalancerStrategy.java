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
package io.gravitee.gateway.jupiter.core.v4.endpoint.lb;

import io.gravitee.gateway.jupiter.core.v4.endpoint.ManagedEndpoint;
import java.util.List;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class NoneLoadBalancerStrategy implements LoadBalancerStrategy {

    private final List<ManagedEndpoint> endpoints;

    public NoneLoadBalancerStrategy(List<ManagedEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public ManagedEndpoint next() {
        if (!endpoints.isEmpty()) {
            return endpoints.get(0);
        }

        return null;
    }
}
