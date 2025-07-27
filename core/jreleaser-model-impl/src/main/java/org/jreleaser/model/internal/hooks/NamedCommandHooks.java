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
 * @since 1.2.0
 */
public final class NamedCommandHooks extends AbstractActivatable<NamedCommandHooks> implements Domain {
    private static final long serialVersionUID = -5644580395019320416L;

    private final List<CommandHook> before = new ArrayList<>();
    private final List<CommandHook> success = new ArrayList<>();
    private final List<CommandHook> failure = new ArrayList<>();
    private final Map<String, String> environment = new LinkedHashMap<>();
    private final Matrix matrix = new Matrix();

    private String name;
    private String condition;
    private Boolean applyDefaultMatrix;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.NamedCommandHooks immutable = new org.jreleaser.model.api.hooks.NamedCommandHooks() {
        private static final long serialVersionUID = -5941160336124473447L;

        private List<? extends org.jreleaser.model.api.hooks.CommandHook> before;
        private List<? extends org.jreleaser.model.api.hooks.CommandHook> success;
        private List<? extends org.jreleaser.model.api.hooks.CommandHook> failure;

        @Override
        public String getName() {
            return NamedCommandHooks.this.getName();
        }

        @Override
        public String getCondition() {
            return NamedCommandHooks.this.getCondition();
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getBefore() {
            if (null == before) {
                before = NamedCommandHooks.this.before.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return before;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getSuccess() {
            if (null == success) {
                success = NamedCommandHooks.this.success.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return success;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getFailure() {
            if (null == failure) {
                failure = NamedCommandHooks.this.failure.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return failure;
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(NamedCommandHooks.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return NamedCommandHooks.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public Active getActive() {
            return NamedCommandHooks.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return NamedCommandHooks.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Collections.unmodifiableMap(NamedCommandHooks.this.asMap(full));
        }
    };

    public NamedCommandHooks() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.hooks.NamedCommandHooks asImmutable() {
        return immutable;
    }

    @Override
    public void merge(NamedCommandHooks source) {
        super.merge(source);
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

    public List<CommandHook> getBefore() {
        return before;
    }

    public void setBefore(List<CommandHook> before) {
        this.before.clear();
        this.before.addAll(before);
    }

    public List<CommandHook> getSuccess() {
        return success;
    }

    public void setSuccess(List<CommandHook> success) {
        this.success.clear();
        this.success.addAll(success);
    }

    public List<CommandHook> getFailure() {
        return failure;
    }

    public void setFailure(List<CommandHook> failure) {
        this.failure.clear();
        this.failure.addAll(failure);
    }

    public void addBefore(CommandHook hook) {
        if (null != hook) {
            this.before.add(hook);
        }
    }

    public void addSuccess(CommandHook hook) {
        if (null != hook) {
            this.success.add(hook);
        }
    }

    public void addFailure(CommandHook hook) {
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
        map.put("name", name);
        map.put("condition", condition);
        map.put("environment", environment);
        matrix.asMap(map);

        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        int i = 0;
        for (CommandHook hook : getBefore()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("before", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (CommandHook hook : getSuccess()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("success", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (CommandHook hook : getFailure()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("failure", m);

        return map;
    }
}
