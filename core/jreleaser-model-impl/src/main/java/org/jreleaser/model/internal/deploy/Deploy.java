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
package org.jreleaser.model.internal.deploy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.deploy.maven.Maven;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class Deploy extends AbstractActivatable<Deploy> implements Domain {
    private static final long serialVersionUID = 1065361758727406904L;

    private final Maven maven = new Maven();

    @JsonIgnore
    private final org.jreleaser.model.api.deploy.Deploy immutable = new org.jreleaser.model.api.deploy.Deploy() {
        private static final long serialVersionUID = 487506438939211307L;

        @Override
        public org.jreleaser.model.api.deploy.maven.Maven getMaven() {
            return maven.asImmutable();
        }

        @Override
        public Active getActive() {
            return Deploy.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Deploy.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Deploy.this.asMap(full));
        }
    };

    public Deploy() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.deploy.Deploy asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Deploy source) {
        super.merge(source);
        setMaven(source.maven);
    }

    @Override
    public boolean isSet() {
        return super.isSet() || maven.isSet();
    }

    public Maven getMaven() {
        return maven;
    }

    public void setMaven(Maven maven) {
        this.maven.merge(maven);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("maven", maven.asMap(full));
        return map;
    }
}
