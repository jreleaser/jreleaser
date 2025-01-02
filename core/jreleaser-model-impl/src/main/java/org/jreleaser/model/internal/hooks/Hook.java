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
package org.jreleaser.model.internal.hooks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.Matrix;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public interface Hook extends Domain, Activatable {
    Filter getFilter();

    void setFilter(Filter filter);

    Set<String> getPlatforms();

    void setPlatforms(Set<String> platforms);

    boolean isContinueOnError();

    void setContinueOnError(Boolean continueOnError);

    boolean isContinueOnErrorSet();

    boolean isVerbose();

    void setVerbose(Boolean verbose);

    boolean isVerboseSet();

    String getCondition();

    void setCondition(String condition);

    Map<String, String> getEnvironment();

    void setEnvironment(Map<String, String> environment);

    void addEnvironment(Map<String, String> environment);

    boolean isApplyDefaultMatrixSet();

    boolean isApplyDefaultMatrix();

    void setApplyDefaultMatrix(Boolean applyDefaultMatrix);

    Matrix getMatrix();

    void setMatrix(Matrix matrix);

    class Filter extends AbstractModelObject<Filter> implements Domain {
        private static final long serialVersionUID = 8811064830998012126L;

        private final Set<String> includes = new LinkedHashSet<>();
        private final Set<String> excludes = new LinkedHashSet<>();

        @JsonIgnore
        private final org.jreleaser.model.api.hooks.Hook.Filter immutable = new org.jreleaser.model.api.hooks.Hook.Filter() {
            private static final long serialVersionUID = -6995455963355646704L;

            @Override
            public Set<String> getIncludes() {
                return unmodifiableSet(includes);
            }

            @Override
            public Set<String> getExcludes() {
                return unmodifiableSet(excludes);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Hook.Filter.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.hooks.Hook.Filter asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Filter source) {
            setIncludes(merge(this.includes, source.includes));
            setExcludes(merge(this.excludes, source.excludes));
        }

        public Set<String> getResolvedIncludes() {
            return includes.stream()
                .map(String::toLowerCase)
                .collect(toSet());
        }

        public Set<String> getResolvedExcludes() {
            return excludes.stream()
                .map(String::toLowerCase)
                .collect(toSet());
        }

        public Set<String> getIncludes() {
            return includes;
        }

        public void setIncludes(Set<String> includes) {
            this.includes.clear();
            this.includes.addAll(includes);
        }

        public Set<String> getExcludes() {
            return excludes;
        }

        public void setExcludes(Set<String> excludes) {
            this.excludes.clear();
            this.excludes.addAll(excludes);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("includes", getResolvedIncludes());
            map.put("excludes", getResolvedExcludes());
            return map;
        }
    }
}
