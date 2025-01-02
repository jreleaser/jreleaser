/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.model.api.common;

import org.jreleaser.util.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isTrue;
import static org.jreleaser.util.StringUtils.splitValue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface ExtraProperties extends Serializable {
    String getPrefix();

    Map<String, Object> getExtraProperties();

    default Map<String, Object> getResolvedExtraProperties() {
        return getResolvedExtraProperties(getPrefix());
    }

    default Map<String, Object> getResolvedExtraProperties(String prefix) {
        Map<String, Object> props = new LinkedHashMap<>();

        getExtraProperties().forEach((key, value) -> {
            if (null == value) return;

            boolean split = key.endsWith("_split");
            String k = key;
            Object v = value;

            if (split) {
                k = key.substring(0, key.length() - "_split".length());
                v = splitValue(String.valueOf(value));
            }

            if (key.startsWith(prefix)) {
                props.put(k, v);
            } else {
                props.put(prefix + StringUtils.capitalize(k), v);
            }
        });

        return props;
    }

    default String getExtraProperty(String key) {
        if (getExtraProperties().containsKey(key)) {
            return String.valueOf(getExtraProperties().get(key));
        }
        return null;
    }

    default boolean extraPropertyIsTrue(String key) {
        return getExtraProperties().containsKey(key) && isTrue(getExtraProperties().get(key));
    }
}
