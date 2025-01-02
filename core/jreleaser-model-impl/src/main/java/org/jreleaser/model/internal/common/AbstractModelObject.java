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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractModelObject<S extends AbstractModelObject<S>> implements ModelObject<S> {
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
            s1.addAll(incoming);
        }

        return s1;
    }

    protected <T> Map<String, T> merge(Map<String, T> existing, Map<String, T> incoming) {
        Map<String, T> m1 = new LinkedHashMap<>();
        if (null != existing) m1.putAll(existing);
        if (null != incoming && !incoming.isEmpty()) {
            Map<String, T> m2 = new LinkedHashMap<>(incoming);
            if (null != existing) m2.keySet().removeAll(existing.keySet());
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
                if (null != existing && existing.containsKey(key)) {
                    T value = existing.get(key);
                    value.merge(e.getValue());
                } else {
                    m1.put(key, e.getValue());
                }
            }
        }

        return m1;
    }

    protected <E extends ModelObject<E>, T extends E> Set<T> mergeModel(Set<T> existing, Set<T> incoming) {
        Set<T> s1 = new LinkedHashSet<>();
        if (null != existing) s1.addAll(existing);
        if (null != incoming && !incoming.isEmpty()) {
            for (T e : incoming) {
                if (null != existing && existing.contains(e)) {
                    existing.stream()
                        .filter(a -> a.equals(e))
                        .findFirst()
                        .ifPresent(v -> v.merge(e));
                } else {
                    s1.add(e);
                }
            }
        }

        return s1;
    }
}
