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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifact extends AbstractDomain {
    private String path;
    private String hash;
    private String osClassifier;
    private String javaVersion;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getOsClassifier() {
        return osClassifier;
    }

    public void setOsClassifier(String osClassifier) {
        this.osClassifier = osClassifier;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", path);
        map.put("hash", hash);
        map.put("osClassifier", osClassifier);
        map.put("javaVersion", javaVersion);
        return map;
    }
}
