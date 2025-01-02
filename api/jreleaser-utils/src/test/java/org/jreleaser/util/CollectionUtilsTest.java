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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionUtilsTest {
    @Test
    void testMapBuilderAdDelegate() {
        // given:
        Map<String, String> delegate = new LinkedHashMap<>();
        Map<String, String> map = CollectionUtils.map(delegate);

        // when:
        map.put("key", "value");

        // then:
        assertEquals(1, map.size());
        assertEquals(map.keySet(), delegate.keySet());
        assertEquals(map.values(), delegate.values());
        assertEquals(map.containsKey("key"), delegate.containsKey("key"));
        assertEquals(map.containsValue("value"), delegate.containsValue("value"));

        // when:
        map.remove("key");

        // then:
        assertEquals(0, delegate.size());

        // when:
        map.put("key", "value");
        assertEquals(1, delegate.size());
        map.clear();

        // then:
        assertEquals(0, delegate.size());

        assertEquals(map.hashCode(), delegate.hashCode());
    }

    @Test
    void testListBuilderAndDelegate() {
        // given:
        List<Object> delegate = new ArrayList<>();
        CollectionUtils.ListBuilder<Object> list = CollectionUtils.list(delegate);

        // when:
        list.add(2);
        list.add(0, 0);
        list.set(1, 1);
        list.add("foo");

        // then:
        assertEquals(3, delegate.size());
        assertEquals(asList(0, 1, "foo"), delegate);

        // when:
        list.remove("foo");
        // then:
        assert delegate.equals(new ArrayList<Integer>(asList(0, 1)));

        // when:
        list.clear();
        // then:
        assertEquals(0, delegate.size());

        assert list.hashCode() == delegate.hashCode();

        // when:
        list.e(1);
        // then:
        assertEquals(list.size(), delegate.size());
        assertEquals(list.contains(1), delegate.contains(1));
        assertTrue(() -> list.containsAll(asList(1)));
    }

    @Test
    void testSetBuilderAndDelegate() {
        // given:
        Set<Object> delegate = new LinkedHashSet<>();
        CollectionUtils.SetBuilder<Object> set = CollectionUtils.set(delegate);

        // when:
        set.add(0);
        set.add(1);
        set.add("foo");

        // then:
        assertEquals(3, delegate.size());

        // when:
        set.remove("foo");
        // then:
        assertEquals(2, delegate.size());
        assertTrue(() -> delegate.contains(0));
        assertTrue(() -> delegate.contains(1));

        // when:
        set.clear();
        // then:
        assertEquals(0, delegate.size());

        assert set.hashCode() == delegate.hashCode();

        // when:
        set.e(1);
        // then:
        assertEquals(set.size(), delegate.size());
        assertEquals(set.contains(1), delegate.contains(1));
        assertTrue(() -> set.containsAll(asList(1)));
    }

    @Test
    void testToPropertiesDeep() {
        // given:
        Map<String, Object> map1 = new LinkedHashMap<>(5);
        map1.put("singleKey", "singleValue");
        map1.put("key.string", "string");
        map1.put("key.boolean", true);
        map1.put("key.list", asList(1, 2, 3));
        LinkedHashMap<String, Integer> map2 = new LinkedHashMap<>(2);
        map2.put("one", 1);
        map2.put("two", 2);
        map1.put("key.map", map2);

        // when:
        Properties props = CollectionUtils.toPropertiesDeep(map1);

        // then:
        assertEquals(6, props.keySet().size());
        assertEquals("singleValue", props.getProperty("singleKey"));
        assertEquals("string", props.getProperty("key.string"));
        assertEquals(true, props.get("key.boolean"));
        assertEquals(asList(1, 2, 3), props.get("key.list"));
        assertEquals(1, props.get("key.map.one"));
        assertEquals(2, props.get("key.map.two"));
    }
}
