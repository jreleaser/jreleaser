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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * <p>Utility class that simplifies creating collections in Java.</p>
 * <p><strong>Creating Maps</strong><br/>
 * <pre>
 * Map&lt;String, Object&gt; m = map()
 *     .e("foo", foo)
 *     .e("bar", bar);
 * </pre></p>
 *
 * <p><strong>Creating Lists</strong><br/>
 * <pre>
 * List&lt;String&gt; l = list()
 *     .e("foo")
 *     .e("bar");
 * </pre></p>
 *
 * <p><strong>Creating Maps</strong><br/>
 * <pre>
 * Set&lt;String&gt; s = set()
 *     .e("foo")
 *     .e("bar");
 * </pre></p>
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class CollectionUtils {
    private static final String ERROR_MAP_NULL = "Argument 'map' must not be null";

    private CollectionUtils() {
        // prevent instantiation
    }

    public static void safePut(String key, Map<String, Object> src, Properties dest) {
        if (src.containsKey(key)) {
            dest.put(key, String.valueOf(src.get(key)));
        }
    }

    public static void safePut(String key, Map<String, Object> src, Properties dest, boolean forceKey) {
        if (src.containsKey(key)) {
            dest.put(key, String.valueOf(src.get(key)));
        } else if (forceKey) {
            dest.put(key, "");
        }
    }

    public static void safePut(String key, Map<String, Object> src, Map<String, Object> dest) {
        if (src.containsKey(key)) {
            dest.put(key, src.get(key));
        }
    }

    public static void safePut(String key, Map<String, Object> src, Map<String, Object> dest, boolean forceKey) {
        if (src.containsKey(key)) {
            dest.put(key, src.get(key));
        } else if (forceKey) {
            dest.put(key, "");
        }
    }

    public static void safePut(String key, Object value, Map<String, Object> dest) {
        if (null != value) {
            dest.put(key, value);
        }
    }

    public static void safePut(String key, Object value, Map<String, Object> dest, boolean forceKey) {
        if (null != value) {
            dest.put(key, value);
        } else if (forceKey) {
            dest.put(key, "");
        }
    }

    public static void safePut(String key, Object value, Properties dest) {
        if (null != value) {
            dest.put(key, value);
        }
    }

    public static <T> boolean intersects(Set<T> s1, Set<T> s2) {
        Set<T> intersection = new LinkedHashSet<>(s1);
        intersection.removeAll(s2);
        return intersection.size() != s1.size();
    }

    public static <T> List<T> reverse(List<T> input) {
        List<T> output = new ArrayList<>(input);
        Collections.reverse(output);
        return output;
    }

    public static <T> List<T> reverse(Collection<T> input) {
        List<T> output = new ArrayList<>(input);
        Collections.reverse(output);
        return output;
    }

    @Deprecated
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> Map newMap(Object... keysAndValues) {
        return mapOf(keysAndValues);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> Map mapOf(Object... keysAndValues) {
        if (null == keysAndValues) {
            return Collections.emptyMap();
        }
        if (keysAndValues.length % 2 == 1) {
            throw new IllegalArgumentException("Must have an even number of keys and values");
        }

        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return map;
    }

    @Deprecated
    @SafeVarargs
    public static <T> Set<T> newSet(T... values) {
        return setOf(values);
    }

    @SafeVarargs
    public static <T> Set<T> setOf(T... values) {
        if (null == values) {
            return Collections.emptySet();
        }

        return new HashSet<>(Arrays.asList(values));
    }

    @Deprecated
    @SafeVarargs
    public static <T> List<T> newList(T... values) {
        return listOf(values);
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... values) {
        if (null == values) {
            return Collections.emptyList();
        }

        return new ArrayList<>(Arrays.asList(values));
    }

    public static <K, V> MapBuilder<K, V> map() {
        return map(new LinkedHashMap<>());
    }

    public static <K, V> MapBuilder<K, V> map(Map<K, V> delegate) {
        return new MapBuilder<>(delegate);
    }

    public static <E> ListBuilder<E> list() {
        return list(new ArrayList<>());
    }

    public static <E> ListBuilder<E> list(List<E> delegate) {
        return new ListBuilder<>(delegate);
    }

    public static <E> SetBuilder<E> set() {
        return set(new HashSet<>());
    }

    public static <E> SetBuilder<E> set(Set<E> delegate) {
        return new SetBuilder<>(delegate);
    }

    /**
     * Returns an adapted Map as a Properties instance.
     * <p>
     * The Map is used live, which means changes made to it will affect the
     * Properties instance directly.
     *
     * @param map the Map instance to adapt as a Properties instance
     * @return a new Properties instance backed by the supplied Map.
     */
    public static Properties toProperties(Map<String, Object> map) {
        requireNonNull(map, ERROR_MAP_NULL);
        return new MapToPropertiesAdapter(map);
    }

    /**
     * Creates a Properties instances based on the given Map.
     *
     * @param map the Map instance to convert as a Properties instance
     * @return a new Properties instance based by the supplied Map.
     */
    public static Properties toPropertiesDeep(Map<String, Object> map) {
        requireNonNull(map, ERROR_MAP_NULL);
        Properties properties = new Properties();

        for (Map.Entry<String, Object> e : map.entrySet()) {
            createKey(properties, e.getKey(), e.getValue());
        }

        return properties;
    }

    @SuppressWarnings("unchecked")
    private static void createKey(Properties properties, String key, Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                createKey(properties, key + "." + e.getKey(), e.getValue());
            }
        } else {
            properties.put(key, value);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class MapBuilder<K, V> implements Map<K, V> {
        private final Map<K, V> delegate;

        public MapBuilder(Map<K, V> delegate) {
            this.delegate = delegate;
        }

        public MapBuilder<K, V> e(K k, V v) {
            delegate.put(k, v);
            return this;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(Object o) {
            return delegate.containsKey(o);
        }

        @Override
        public boolean containsValue(Object o) {
            return delegate.containsValue(o);
        }

        @Override
        public V get(Object o) {
            return delegate.get(o);
        }

        @Override
        public V put(K k, V v) {
            return delegate.put(k, v);
        }

        @Override
        public V remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map) {
            delegate.putAll(map);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Set<K> keySet() {
            return delegate.keySet();
        }

        @Override
        public Collection<V> values() {
            return delegate.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return delegate.entrySet();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class ListBuilder<E> implements List<E> {
        private final List<E> delegate;

        public ListBuilder(List<E> delegate) {
            this.delegate = delegate;
        }

        public ListBuilder<E> e(E e) {
            delegate.add(e);
            return this;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] ts) {
            return delegate.toArray(ts);
        }

        @Override
        public boolean add(E e) {
            return delegate.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> objects) {
            return delegate.containsAll(objects);
        }

        @Override
        public boolean addAll(Collection<? extends E> es) {
            return delegate.addAll(es);
        }

        @Override
        public boolean addAll(int i, Collection<? extends E> es) {
            return delegate.addAll(i, es);
        }

        @Override
        public boolean removeAll(Collection<?> objects) {
            return delegate.removeAll(objects);
        }

        @Override
        public boolean retainAll(Collection<?> objects) {
            return delegate.retainAll(objects);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public E get(int i) {
            return delegate.get(i);
        }

        @Override
        public E set(int i, E e) {
            return delegate.set(i, e);
        }

        @Override
        public void add(int i, E e) {
            delegate.add(i, e);
        }

        @Override
        public E remove(int i) {
            return delegate.remove(i);
        }

        @Override
        public int indexOf(Object o) {
            return delegate.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return delegate.lastIndexOf(o);
        }

        @Override
        public ListIterator<E> listIterator() {
            return delegate.listIterator();
        }

        @Override
        public ListIterator<E> listIterator(int i) {
            return delegate.listIterator(i);
        }

        @Override
        public List<E> subList(int i, int i1) {
            return delegate.subList(i, i1);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class SetBuilder<E> implements Set<E> {
        private final Set<E> delegate;

        public SetBuilder(Set<E> delegate) {
            this.delegate = delegate;
        }

        public SetBuilder<E> e(E e) {
            delegate.add(e);
            return this;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] ts) {
            return delegate.toArray(ts);
        }

        @Override
        public boolean add(E e) {
            return delegate.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> objects) {
            return delegate.containsAll(objects);
        }

        @Override
        public boolean addAll(Collection<? extends E> es) {
            return delegate.addAll(es);
        }

        @Override
        public boolean retainAll(Collection<?> objects) {
            return delegate.retainAll(objects);
        }

        @Override
        public boolean removeAll(Collection<?> objects) {
            return delegate.removeAll(objects);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class MapToPropertiesAdapter extends Properties {
        private static final long serialVersionUID = -5465075555013344300L;
        private final transient Map<String, Object> map;

        private MapToPropertiesAdapter(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public synchronized Object setProperty(String key, String value) {
            return map.put(key, value);
        }

        @Override
        public String getProperty(String key) { // lgtm [java/unsynchronized-getter]
            Object value = map.get(key);
            return null != value ? String.valueOf(value) : null;
        }

        @Override
        public String getProperty(String key, String defaultValue) { // lgtm [java/unsynchronized-getter]
            Object value = map.get(key);
            return null != value ? String.valueOf(value) : defaultValue;
        }

        @Override
        public Enumeration<?> propertyNames() {
            return keys();
        }

        @Override
        public Set<String> stringPropertyNames() {
            return map.keySet();
        }

        @Override
        public synchronized int size() {
            return map.size();
        }

        @Override
        public synchronized boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return new Enumeration<Object>() {
                private final Iterator<String> keys = new ArrayList<>(map.keySet()).iterator();

                @Override
                public boolean hasMoreElements() {
                    return keys.hasNext();
                }

                @Override
                public String nextElement() {
                    return keys.next();
                }
            };
        }

        @Override
        public synchronized Enumeration<Object> elements() {
            return new Enumeration<Object>() {
                private final Iterator<Object> values = new ArrayList<>(map.values()).iterator();

                @Override
                public boolean hasMoreElements() {
                    return values.hasNext();
                }

                @Override
                public Object nextElement() {
                    return values.next();
                }
            };
        }

        @Override
        public synchronized boolean contains(Object value) {
            return map.containsValue(value);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public synchronized boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public synchronized Object get(Object key) {
            return map.get(key);
        }

        @Override
        public synchronized Object put(Object key, Object value) {
            return map.put(String.valueOf(key), value);
        }

        @Override
        public synchronized Object remove(Object key) {
            return map.remove(key);
        }

        @Override
        public synchronized void putAll(Map<?, ?> t) {
            map.putAll((Map<String, Object>) t);
        }

        @Override
        public synchronized void clear() {
            map.clear();
        }

        @Override
        public Set<Object> keySet() {
            return new LinkedHashSet<>(map.keySet());
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            Set<Map.Entry<Object, Object>> set = new LinkedHashSet<>((Set) map.entrySet());
            return new LinkedHashSet<>(set);
        }

        @Override
        public Collection<Object> values() {
            return map.values();
        }

        @Override
        @SuppressWarnings("NoClone")
        public synchronized Object clone() {
            Map<String, Object> m = new LinkedHashMap<>(map);
            return new MapToPropertiesAdapter(m);
        }
    }
}
