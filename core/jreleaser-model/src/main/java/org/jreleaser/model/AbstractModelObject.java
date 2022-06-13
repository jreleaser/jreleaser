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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractModelObject<S extends AbstractModelObject<S>> implements ModelObject<S> {
    @JsonIgnore
    protected boolean frozen;

    @Override
    public void freeze() {
        frozen = true;
    }

    public void mutate(Mutator mutator) {
        if (null == mutator) return;

        boolean isFrozen = frozen;
        try {
            frozen = false;
            mutator.mutate();
        } finally {
            frozen = isFrozen;
        }
    }

    protected void freezeCheck() {
        if (frozen) throw new UnsupportedOperationException();
    }

    protected <T> List<T> freezeWrap(List<T> list) {
        return frozen ? Collections.unmodifiableList(list) : list;
    }

    protected <T> Set<T> freezeWrap(Set<T> set) {
        return frozen ? Collections.unmodifiableSet(set) : set;
    }

    protected <K, V> Map<K, V> freezeWrap(Map<K, V> map) {
        return frozen ? Collections.unmodifiableMap(map) : map;
    }

    protected Properties freezeWrap(Properties props) {
        return frozen ? new Properties(props) : props;
    }

    protected String merge(String existing, String incoming) {
        return isNotBlank(incoming) ? incoming : existing;
    }

    protected Boolean merge(Boolean existing, Boolean incoming) {
        return null != incoming ? incoming : existing;
    }

    protected Integer merge(Integer existing, Integer incoming) {
        return null != incoming ? incoming : existing;
    }

    protected int merge(int existing, int incoming) {
        return 0 != incoming ? incoming : existing;
    }

    protected <T> T merge(T existing, T incoming) {
        return null != incoming ? incoming : existing;
    }

    protected <T> List<T> merge(List<T> existing, List<T> incoming) {
        List<T> l1 = new ArrayList<>();
        if (null != existing) l1.addAll(existing);
        if (null != incoming && !incoming.isEmpty()) {
            List<T> l2 = new ArrayList<>(incoming);
            l2.removeAll(l1);
            l1.addAll(l2);
        }

        return l1;
    }

    protected <T> Set<T> merge(Set<T> existing, Set<T> incoming) {
        Set<T> s1 = new LinkedHashSet<>();
        if (null != existing) s1.addAll(existing);
        if (null != incoming && !incoming.isEmpty()) {
            Set<T> s2 = new LinkedHashSet<>(incoming);
            s2.removeAll(s1);
            s1.addAll(s2);
        }

        return s1;
    }

    protected <T> Map<String, T> merge(Map<String, T> existing, Map<String, T> incoming) {
        Map<String, T> m1 = new LinkedHashMap<>();
        if (null != existing) m1.putAll(existing);
        if (null != incoming && !incoming.isEmpty()) {
            Map<String, T> m2 = new LinkedHashMap<>(incoming);
            m2.keySet().removeAll(existing.keySet());
            m1.putAll(m2);
        }

        return m1;
    }

    protected <E extends ModelObject<E>, T extends E> Map<String, T> mergeModel(Map<String, T> existing, Map<String, T> incoming) {
        Map<String, T> m1 = new LinkedHashMap<>();
        if (null != existing) m1.putAll(existing);
        if (null != incoming && !incoming.isEmpty()) {
            for (Map.Entry<String, T> e : incoming.entrySet()) {
                String key = e.getKey();
                if (existing.containsKey(key)) {
                    T value = existing.get(key);
                    value.merge(e.getValue());
                } else {
                    m1.put(key, e.getValue());
                }
            }
        }

        return m1;
    }

    public interface Mutator {
        void mutate() throws RuntimeException;
    }
}
