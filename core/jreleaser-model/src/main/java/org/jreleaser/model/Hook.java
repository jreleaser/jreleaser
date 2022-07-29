/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public interface Hook extends Domain, Activatable {
    Filter getFilter();

    void setFilter(Filter filter);

    boolean isContinueOnError();

    void setContinueOnError(Boolean continueOnError);

    boolean isContinueOnErrorSet();

    class Filter extends AbstractModelObject<Filter> implements Domain {
        private final Set<String> includes = new LinkedHashSet<>();
        private final Set<String> excludes = new LinkedHashSet<>();

        @Override
        public void merge(Filter source) {
            freezeCheck();
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
            return freezeWrap(includes);
        }

        public void setIncludes(Set<String> includes) {
            freezeCheck();
            this.includes.clear();
            this.includes.addAll(includes);
        }

        public Set<String> getExcludes() {
            return freezeWrap(excludes);
        }

        public void setExcludes(Set<String> excludes) {
            freezeCheck();
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
