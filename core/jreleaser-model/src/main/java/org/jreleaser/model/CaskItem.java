/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class CaskItem implements Domain {
    private final List<String> items = new ArrayList<>();
    private String name;

    public CaskItem(String name, List<String> items) {
        this.name = name;
        this.items.addAll(items);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public void addItems(List<String> item) {
        this.items.addAll(item);
    }

    public void addItem(String item) {
        if (isNotBlank(item)) {
            this.items.add(item.trim());
        }
    }

    public void removeItem(String item) {
        if (isNotBlank(item)) {
            this.items.remove(item.trim());
        }
    }

    public boolean getHasItems() {
        return !items.isEmpty();
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        return asMap();
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(name, items);
        return map;
    }
}
