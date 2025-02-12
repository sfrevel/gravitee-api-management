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
package io.gravitee.definition.model.v4;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@RequiredArgsConstructor
@Getter
@Schema(name = "ConnectorFeatureV4")
public enum ConnectorFeature {
    LIMIT("limit"),
    RESUME("resume");

    private static final Map<String, ConnectorFeature> LABELS_MAP = Map.of(LIMIT.label, LIMIT, RESUME.label, RESUME);

    @JsonValue
    private final String label;

    public static ConnectorFeature fromLabel(final String label) {
        if (label != null) {
            return LABELS_MAP.get(label);
        }
        return null;
    }
}
