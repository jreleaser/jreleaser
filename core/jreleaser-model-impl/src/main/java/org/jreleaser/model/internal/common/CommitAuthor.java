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
package org.jreleaser.model.internal.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class CommitAuthor extends AbstractModelObject<CommitAuthor> implements Domain {
    private static final long serialVersionUID = 8282090922077012974L;

    private String email;
    private String name;

    @JsonIgnore
    private final org.jreleaser.model.api.common.CommitAuthor immutable = new org.jreleaser.model.api.common.CommitAuthor() {
        private static final long serialVersionUID = -4344080671093237233L;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(CommitAuthor.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.common.CommitAuthor asImmutable() {
        return immutable;
    }

    @Override
    public void merge(CommitAuthor source) {
        this.email = merge(this.email, source.email);
        this.name = merge(this.name, source.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", getName());
        map.put("email", getEmail());
        return map;
    }
}
