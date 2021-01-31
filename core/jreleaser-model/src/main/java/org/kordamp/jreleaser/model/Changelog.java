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
package org.kordamp.jreleaser.model;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.kordamp.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Changelog extends AbstractDomain {
    private Boolean enabled;
    private boolean enabledSet;
    private Sort sort = Sort.ASC;
    private File external;

    void setAll(Changelog changelog) {
        this.enabled = changelog.enabled;
        this.enabledSet = changelog.enabledSet;
        this.sort = changelog.sort;
        this.external = changelog.external;
    }

    public Boolean isEnabled() {
        return enabled != null && enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabledSet = true;
        this.enabled = enabled;
    }

    public boolean isEnabledSet() {
        return enabledSet;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public void setSort(String sort) {
        if (isNotBlank(sort)) {
            setSort(Sort.valueOf(sort.toUpperCase()));
        }
    }

    public File getExternal() {
        return external;
    }

    public void setExternal(File external) {
        this.external = external;
    }

    public Map<String, Object> asMap() {
        if (!isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("sort", sort);
        map.put("external", external);
        return map;
    }

    public enum Sort {
        ASC, DESC
    }
}
