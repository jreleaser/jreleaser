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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.Matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class ScriptHooks extends AbstractActivatable<ScriptHooks> implements Domain {
    private static final long serialVersionUID = -4297448990828592276L;

    private final List<ScriptHook> before = new ArrayList<>();
    private final List<ScriptHook> success = new ArrayList<>();
    private final List<ScriptHook> failure = new ArrayList<>();
    private final Map<String, String> environment = new LinkedHashMap<>();
    private final Matrix matrix = new Matrix();

    private String condition;
    private Boolean applyDefaultMatrix;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.ScriptHooks immutable = new org.jreleaser.model.api.hooks.ScriptHooks() {
        private static final long serialVersionUID = 5161877226451497599L;

        private List<? extends org.jreleaser.model.api.hooks.ScriptHook> before;
        private List<? extends org.jreleaser.model.api.hooks.ScriptHook> success;
        private List<? extends org.jreleaser.model.api.hooks.ScriptHook> failure;

        @Override
        public String getCondition() {
            return ScriptHooks.this.getCondition();
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.ScriptHook> getBefore() {
            if (null == before) {
                before = ScriptHooks.this.before.stream()
                    .map(ScriptHook::asImmutable)
                    .collect(toList());
            }
            return before;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.ScriptHook> getSuccess() {
            if (null == success) {
                success = ScriptHooks.this.success.stream()
                    .map(ScriptHook::asImmutable)
                    .collect(toList());
            }
            return success;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.ScriptHook> getFailure() {
            if (null == failure) {
                failure = ScriptHooks.this.failure.stream()
                    .map(ScriptHook::asImmutable)
                    .collect(toList());
            }
            return failure;
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(ScriptHooks.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return ScriptHooks.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public Active getActive() {
            return ScriptHooks.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ScriptHooks.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Collections.unmodifiableMap(ScriptHooks.this.asMap(full));
        }
    };

    public ScriptHooks() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.hooks.ScriptHooks asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ScriptHooks source) {
        super.merge(source);
        this.condition = merge(this.condition, source.condition);
        setBefore(merge(this.before, source.before));
        setSuccess(merge(this.success, source.success));
        setFailure(merge(this.failure, source.failure));
        setEnvironment(merge(this.environment, source.getEnvironment()));
        setMatrix(source.matrix);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            !before.isEmpty() ||
            !success.isEmpty() ||
            !failure.isEmpty();
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<ScriptHook> getBefore() {
        return before;
    }

    public void setBefore(List<ScriptHook> before) {
        this.before.clear();
        this.before.addAll(before);
    }

    public List<ScriptHook> getSuccess() {
        return success;
    }

    public void setSuccess(List<ScriptHook> success) {
        this.success.clear();
        this.success.addAll(success);
    }

    public List<ScriptHook> getFailure() {
        return failure;
    }

    public void setFailure(List<ScriptHook> failure) {
        this.failure.clear();
        this.failure.addAll(failure);
    }

    public void addBefore(ScriptHook hook) {
        if (null != hook) {
            this.before.add(hook);
        }
    }

    public void addSuccess(ScriptHook hook) {
        if (null != hook) {
            this.success.add(hook);
        }
    }

    public void addFailure(ScriptHook hook) {
        if (null != hook) {
            this.failure.add(hook);
        }
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment.clear();
        this.environment.putAll(environment);
    }

    public boolean isApplyDefaultMatrixSet() {
        return null != applyDefaultMatrix;
    }

    public boolean isApplyDefaultMatrix() {
        return null != applyDefaultMatrix && applyDefaultMatrix;
    }

    public void setApplyDefaultMatrix(Boolean applyDefaultMatrix) {
        this.applyDefaultMatrix = applyDefaultMatrix;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix.merge(matrix);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("condition", condition);
        map.put("environment", environment);
        matrix.asMap(map);

        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        int i = 0;
        for (ScriptHook hook : getBefore()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("before", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (ScriptHook hook : getSuccess()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("success", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (ScriptHook hook : getFailure()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("failure", m);

        return map;
    }
}
