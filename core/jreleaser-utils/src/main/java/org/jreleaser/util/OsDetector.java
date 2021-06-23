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
package org.jreleaser.util;

import kr.motd.maven.os.Detector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class OsDetector extends Detector {
    private final Map<String, String> props = new LinkedHashMap<>();

    public OsDetector() {
        Properties p = new Properties();
        p.put("failOnUnknownOS", "false");
        detect(p, Collections.emptyList());
        p.stringPropertyNames().forEach(k -> props.put(k, p.getProperty(k)));
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(props);
    }

    public String get(String key) {
        return props.get(key);
    }

    @Override
    protected void log(String message) {
        // quiet
    }

    @Override
    protected void logProperty(String name, String value) {
        // quiet
    }
}
