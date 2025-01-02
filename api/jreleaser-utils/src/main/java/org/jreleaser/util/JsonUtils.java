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
package org.jreleaser.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JsonUtils {
    private JsonUtils() {
        //noop
    }

    public static JsonNode merge(JsonNode inputNode, JsonNode updateNode) {
        if (null == updateNode) return inputNode;

        Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode inputValue = inputNode.get(fieldName);
            JsonNode updateValue = updateNode.get(fieldName);

            if (null != inputValue && inputValue.isArray() &&
                null != updateValue && updateValue.isArray()) {
                for (int i = 0; i < updateValue.size(); i++) {
                    JsonNode updatedChildNode = updateValue.get(i);
                    if (inputValue.size() <= i) {
                        ((ArrayNode) inputValue).add(updatedChildNode);
                    }
                    JsonNode childNodeToBeUpdated = inputValue.get(i);
                    merge(childNodeToBeUpdated, updatedChildNode);
                }
            } else if (null != inputValue && inputValue.isObject()) {
                merge(inputValue, updateValue);
            } else {
                if (inputNode instanceof ObjectNode) {
                    ((ObjectNode) inputNode).replace(fieldName, updateValue);
                }
            }
        }

        return inputNode;
    }
}
