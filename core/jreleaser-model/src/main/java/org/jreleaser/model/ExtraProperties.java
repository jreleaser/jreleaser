/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.model;

import org.jreleaser.util.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.splitValue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface ExtraProperties extends Serializable {
    String getPrefix();

    Map<String, Object> getExtraProperties();

    void setExtraProperties(Map<String, Object> properties);

    void addExtraProperties(Map<String, Object> properties);

    default void addExtraProperty(String key, Object value) {
        if ((value instanceof CharSequence && isNotBlank(String.valueOf(value))) || null != value) {
            getExtraProperties().put(key, value);
        }
    }

    default Map<String, Object> getResolvedExtraProperties() {
        Map<String, Object> props = new LinkedHashMap<>();

        getExtraProperties().forEach((key, value) -> {
            if (null == value) return;

            String prefix = getPrefix();

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
}
