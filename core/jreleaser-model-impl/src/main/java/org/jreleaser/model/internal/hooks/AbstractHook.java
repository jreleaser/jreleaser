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
package org.jreleaser.model.internal.hooks;

import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Matrix;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public abstract class AbstractHook<S extends AbstractHook<S>> extends AbstractActivatable<S> implements Hook {
    private static final long serialVersionUID = 6507186145048072581L;

    private final Map<String, String> environment = new LinkedHashMap<>();
    private final Set<String> platforms = new LinkedHashSet<>();
    private final Filter filter = new Filter();
    protected final Matrix matrix = new Matrix();

    protected Boolean continueOnError;
    protected Boolean verbose;
    protected String condition;
    protected Boolean applyDefaultMatrix;

    @Override
    public void merge(S source) {
        super.merge(source);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        this.verbose = merge(this.verbose, source.verbose);
        this.condition = merge(this.condition, source.condition);
        setFilter(source.getFilter());
        setPlatforms(merge(this.platforms, source.getPlatforms()));
        setEnvironment(merge(this.environment, source.getEnvironment()));
        setMatrix(source.matrix);
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
    public String getCondition() {
        return condition;
    }

    @Override
    public void setCondition(String condition) {
        this.condition = condition;
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
    public Set<String> getPlatforms() {
        return platforms;
    }

    @Override
    public void setPlatforms(Set<String> platforms) {
        this.platforms.clear();
        this.platforms.addAll(platforms);
    }

    @Override
    public Map<String, String> getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(Map<String, String> environment) {
        this.environment.clear();
        this.environment.putAll(environment);
    }

    @Override
    public void addEnvironment(Map<String, String> environment) {
        this.environment.putAll(environment);
    }

    @Override
    public boolean isApplyDefaultMatrixSet() {
        return null != applyDefaultMatrix;
    }

    @Override
    public boolean isApplyDefaultMatrix() {
        return null != applyDefaultMatrix && applyDefaultMatrix;
    }

    @Override
    public void setApplyDefaultMatrix(Boolean applyDefaultMatrix) {
        this.applyDefaultMatrix = applyDefaultMatrix;
    }

    @Override
    public Matrix getMatrix() {
        return matrix;
    }

    @Override
    public void setMatrix(Matrix matrix) {
        this.matrix.merge(matrix);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("continueOnError", isContinueOnError());
        map.put("verbose", isVerbose());
        map.put("platforms", platforms);
        map.put("condition", condition);
        Map<String, Object> filterAsMap = filter.asMap(full);
        if (full || !filterAsMap.isEmpty()) {
            map.put("filter", filterAsMap);
        }
        map.put("environment", environment);
        matrix.asMap(map);
        asMap(full, map);

        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> map);
}
