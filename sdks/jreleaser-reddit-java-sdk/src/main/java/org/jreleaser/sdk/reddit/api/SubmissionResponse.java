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
package org.jreleaser.sdk.reddit.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionResponse {
    private JsonWrapper json;

    public JsonWrapper getJson() {
        return json;
    }

    public void setJson(JsonWrapper json) {
        this.json = json;
    }

    public boolean hasErrors() {
        return json != null && json.getErrors() != null && !json.getErrors().isEmpty();
    }

    public List<Object> getErrors() {
        return json != null ? json.getErrors() : null;
    }

    public Map<String, Object> getData() {
        return json != null ? json.getData() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonWrapper {
        private List<Object> errors;
        private Map<String, Object> data;

        public List<Object> getErrors() {
            return errors;
        }

        public void setErrors(List<Object> errors) {
            this.errors = errors;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}