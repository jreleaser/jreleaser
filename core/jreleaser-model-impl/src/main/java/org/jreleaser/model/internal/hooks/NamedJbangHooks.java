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
import org.jreleaser.model.internal.common.MatrixAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.20.0
 */
public final class NamedJbangHooks extends AbstractActivatable<NamedJbangHooks> implements Domain, JbangHookProvider, MatrixAware {
    private static final long serialVersionUID = 8884768091233057014L;

    private final List<JbangHook> before = new ArrayList<>();
    private final List<JbangHook> success = new ArrayList<>();
    private final List<JbangHook> failure = new ArrayList<>();
    private final Map<String, String> environment = new LinkedHashMap<>();
    private final Matrix matrix = new Matrix();

    private String version;
    private String script;
    private String name;
    private String condition;
    private Boolean applyDefaultMatrix;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.NamedJbangHooks immutable = new org.jreleaser.model.api.hooks.NamedJbangHooks() {
        private static final long serialVersionUID = 6613588209546503679L;

        private List<? extends org.jreleaser.model.api.hooks.JbangHook> before;
        private List<? extends org.jreleaser.model.api.hooks.JbangHook> success;
        private List<? extends org.jreleaser.model.api.hooks.JbangHook> failure;

        @Override
        public String getVersion() {
            return NamedJbangHooks.this.getVersion();
        }

        @Override
        public String getScript() {
            return NamedJbangHooks.this.getScript();
        }

        @Override
        public String getName() {
            return NamedJbangHooks.this.getName();
        }

        @Override
        public String getCondition() {
            return NamedJbangHooks.this.getCondition();
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.JbangHook> getBefore() {
            if (null == before) {
                before = NamedJbangHooks.this.before.stream()
                    .map(JbangHook::asImmutable)
                    .collect(toList());
            }
            return before;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.JbangHook> getSuccess() {
            if (null == success) {
                success = NamedJbangHooks.this.success.stream()
                    .map(JbangHook::asImmutable)
                    .collect(toList());
            }
            return success;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.JbangHook> getFailure() {
            if (null == failure) {
                failure = NamedJbangHooks.this.failure.stream()
                    .map(JbangHook::asImmutable)
                    .collect(toList());
            }
            return failure;
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(NamedJbangHooks.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return NamedJbangHooks.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public Active getActive() {
            return NamedJbangHooks.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return NamedJbangHooks.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Collections.unmodifiableMap(NamedJbangHooks.this.asMap(full));
        }
    };

    public NamedJbangHooks() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.hooks.NamedJbangHooks asImmutable() {
        return immutable;
    }

    @Override
    public void merge(NamedJbangHooks source) {
        super.merge(source);
        this.version = merge(this.version, source.version);
        this.script = merge(this.script, source.script);
        this.name = merge(this.name, source.name);
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<JbangHook> getBefore() {
        return before;
    }

    public void setBefore(List<JbangHook> before) {
        this.before.clear();
        this.before.addAll(before);
    }

    public List<JbangHook> getSuccess() {
        return success;
    }

    public void setSuccess(List<JbangHook> success) {
        this.success.clear();
        this.success.addAll(success);
    }

    public List<JbangHook> getFailure() {
        return failure;
    }

    public void setFailure(List<JbangHook> failure) {
        this.failure.clear();
        this.failure.addAll(failure);
    }

    public void addBefore(JbangHook hook) {
        if (null != hook) {
            this.before.add(hook);
        }
    }

    public void addSuccess(JbangHook hook) {
        if (null != hook) {
            this.success.add(hook);
        }
    }

    public void addFailure(JbangHook hook) {
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
        map.put("name", name);
        map.put("condition", condition);
        map.put("environment", environment);
        map.put("version", version);
        map.put("script", script);
        matrix.asMap(map);

        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        int i = 0;
        for (JbangHook hook : getBefore()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("before", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (JbangHook hook : getSuccess()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("success", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (JbangHook hook : getFailure()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("failure", m);

        return map;
    }
}
