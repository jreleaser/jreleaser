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
package org.jreleaser.model.internal.hooks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractModelObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public abstract class AbstractHook<S extends AbstractHook<S>> extends AbstractModelObject<S> implements Hook {
    protected final Filter filter = new Filter();
    protected Boolean continueOnError;
    protected Active active;
    @JsonIgnore
    protected boolean enabled;

    @Override
    public void merge(S source) {
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        setFilter(source.filter);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.put("continueOnError", isContinueOnError());
        Map<String, Object> filterAsMap = filter.asMap(full);
        if (full || !filterAsMap.isEmpty()) {
            map.put("filter", filterAsMap);
        }
        asMap(full, map);

        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> map);
}
