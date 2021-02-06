/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Slot extends AbstractDomain {
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private final List<String> reads = new ArrayList<>();
    private final List<String> writes = new ArrayList<>();
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }

    public void addAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public List<String> getReads() {
        return reads;
    }

    public void setReads(List<String> reads) {
        this.reads.clear();
        this.reads.addAll(reads);
    }

    public void addReads(List<String> read) {
        this.reads.addAll(read);
    }

    public void addRead(String read) {
        if (isNotBlank(read)) {
            this.reads.add(read.trim());
        }
    }

    public void removeRead(String read) {
        if (isNotBlank(read)) {
            this.reads.remove(read.trim());
        }
    }

    public List<String> getWrites() {
        return writes;
    }

    public void setWrites(List<String> writes) {
        this.writes.clear();
        this.writes.addAll(writes);
    }

    public void addWrites(List<String> write) {
        this.writes.addAll(write);
    }

    public void addWrite(String write) {
        if (isNotBlank(write)) {
            this.writes.add(write.trim());
        }
    }

    public void removeWrite(String write) {
        if (isNotBlank(write)) {
            this.writes.remove(write.trim());
        }
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(name, attributes);
        map.put("read", reads);
        map.put("write", writes);
        return map;
    }
}
