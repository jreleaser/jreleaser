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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.CollectionUtils.mapOf;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public final class Matrix extends AbstractModelObject<Matrix> implements Domain {
    private static final long serialVersionUID = 5914850093276859188L;

    private final Map<String, List<String>> vars = new LinkedHashMap<>();
    private final List<Map<String, String>> rows = new ArrayList<>();

    @JsonIgnore
    private final org.jreleaser.model.api.common.Matrix immutable = new org.jreleaser.model.api.common.Matrix() {
        private static final long serialVersionUID = 5995028202549998151L;

        @Override
        public Map<String, List<String>> getVars() {
            return unmodifiableMap(vars);
        }

        @Override
        public List<Map<String, String>> getRows() {
            return unmodifiableList(rows);
        }

        @Override
        public List<Map<String, String>> resolved() {
            return Matrix.this.resolve();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Matrix.this.asMap(full);
        }
    };

    public org.jreleaser.model.api.common.Matrix asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Matrix source) {
        setVars(merge(this.vars, source.vars));
        setRows(merge(this.rows, source.rows));
    }

    public Map<String, List<String>> getVars() {
        return vars;
    }

    public void setVars(Map<String, List<String>> vars) {
        this.vars.clear();
        this.vars.putAll(vars);
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, String>> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        return emptyMap();
    }

    public Map<String, Object> asMap(Map<String, Object> props) {
        if (!isEmpty()) {
            if (hasVars()) {
                props.put("matrix", vars);
            }

            if (hasRows()) {
                int[] count = {0};
                props.put("matrix", rows.stream()
                    .map(item -> mapOf("row " + (count[0]++), item)).collect(toList()));
            }
        }

        return props;
    }

    public boolean isEmpty() {
        return vars.isEmpty() && rows.isEmpty();
    }

    public boolean hasVars() {
        return !vars.isEmpty();
    }

    public boolean hasRows() {
        return !rows.isEmpty();
    }

    public List<Map<String, String>> resolve() {
        if (hasVars()) {
            return unmodifiableList(combinations(vars.values()).stream()
                .map(values -> zip(vars.keySet(), values))
                .collect(toList()));
        }

        return unmodifiableList(rows);
    }

    private <T> List<List<T>> combinations(Collection<List<T>> input) {
        if (input == null) return emptyList();
        return input.stream()
            .filter(list -> list != null && list.size() > 0)
            .map(list -> list.stream().map(Collections::singletonList)
                .collect(toList()))
            .reduce((l1, l2) -> l1.stream()
                .flatMap(i1 -> l2.stream()
                    .map(i2 -> Stream.of(i1, i2)
                        .flatMap(Collection::stream)
                        .collect(toList())))
                .collect(toList()))
            .orElse(emptyList());
    }

    private <K, V> Map<K, V> zip(Collection<K> keys, Collection<V> values) {
        Map<K, V> map = new LinkedHashMap<>();
        Iterator<K> i1 = keys.iterator();
        Iterator<V> i2 = values.iterator();

        while (i1.hasNext() || i2.hasNext()) map.put(i1.next(), i2.next());

        return map;
    }
}
