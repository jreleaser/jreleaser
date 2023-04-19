/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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

import org.jreleaser.model.internal.common.AbstractActivatable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public abstract class AbstractHook<S extends AbstractHook<S>> extends AbstractActivatable<S> implements Hook {
    private static final long serialVersionUID = 6118067369961046144L;

    private final Filter filter = new Filter();
    protected Boolean continueOnError;
    protected Boolean verbose;

    @Override
    public void merge(S source) {
        super.merge(source);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        this.verbose = merge(this.verbose, source.verbose);
        setFilter(source.getFilter());
    }

    @Override
    public boolean isContinueOnError() {
        return null != continueOnError && continueOnError;
    }

    @Override
    public void setContinueOnError(Boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    @Override
    public boolean isContinueOnErrorSet() {
        return null != continueOnError;
    }

    @Override
    public boolean isVerbose() {
        return null != verbose && verbose;
    }

    @Override
    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean isVerboseSet() {
        return null != verbose;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter.merge(filter);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("continueOnError", isContinueOnError());
        map.put("verbose", isVerbose());
        Map<String, Object> filterAsMap = filter.asMap(full);
        if (full || !filterAsMap.isEmpty()) {
            map.put("filter", filterAsMap);
        }
        asMap(full, map);

        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> map);
}
