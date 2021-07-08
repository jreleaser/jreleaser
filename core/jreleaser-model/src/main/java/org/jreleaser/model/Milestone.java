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

import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Milestone implements Domain {
    public static final String MILESTONE_NAME = "MILESTONE_NAME";

    private Boolean close;
    private String name;

    private String cachedName;

    void setAll(Milestone changelog) {
        this.close = changelog.close;
        this.name = changelog.name;
    }

    public String getConfiguredName() {
        return Env.resolve(MILESTONE_NAME, cachedName);
    }

    public String getResolvedName(Map<String, Object> props) {
        if (isBlank(cachedName)) {
            cachedName = getConfiguredName();
        }

        if (isBlank(cachedName)) {
            cachedName = applyTemplate(name, props);
        } else if (cachedName.contains("{{")) {
            cachedName = applyTemplate(cachedName, props);
        }

        return cachedName;
    }

    public String getEffectiveName() {
        return cachedName;
    }

    public Boolean isClose() {
        return close == null || close;
    }

    public void setClose(Boolean close) {
        this.close = close;
    }

    public boolean isCloseSet() {
        return close != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("close", isClose());
        return map;
    }
}
