/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class CommandHooks extends AbstractActivatable<CommandHooks> implements Domain, CommandHookProvider, MatrixAware {
    private static final long serialVersionUID = -279546387946677379L;

    private final Map<String, NamedCommandHooks> groups = new LinkedHashMap<>();
    private final List<CommandHook> before = new ArrayList<>();
    private final List<CommandHook> success = new ArrayList<>();
    private final List<CommandHook> failure = new ArrayList<>();
    private final Map<String, String> environment = new LinkedHashMap<>();
    private final Matrix matrix = new Matrix();

    private String condition;
    private Boolean applyDefaultMatrix;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.CommandHooks immutable = new org.jreleaser.model.api.hooks.CommandHooks() {
        private static final long serialVersionUID = -7008621316266711034L;

        private Map<String, ? extends org.jreleaser.model.api.hooks.NamedCommandHooks> groups;
        private List<? extends org.jreleaser.model.api.hooks.CommandHook> before;
        private List<? extends org.jreleaser.model.api.hooks.CommandHook> success;
        private List<? extends org.jreleaser.model.api.hooks.CommandHook> failure;

        @Override
        public String getCondition() {
            return CommandHooks.this.getCondition();
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.hooks.NamedCommandHooks> getGroups() {
            if (null == groups) {
                groups = CommandHooks.this.groups.values().stream()
                    .map(NamedCommandHooks::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.hooks.NamedCommandHooks::getName, identity()));
            }
            return groups;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getBefore() {
            if (null == before) {
                before = CommandHooks.this.before.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return before;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getSuccess() {
            if (null == success) {
                success = CommandHooks.this.success.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return success;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getFailure() {
            if (null == failure) {
                failure = CommandHooks.this.failure.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return failure;
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(CommandHooks.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return CommandHooks.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public Active getActive() {
            return CommandHooks.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return CommandHooks.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Collections.unmodifiableMap(CommandHooks.this.asMap(full));
        }
    };

    public CommandHooks() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.hooks.CommandHooks asImmutable() {
        return immutable;
    }

    @Override
    public void merge(CommandHooks source) {
        super.merge(source);
        this.condition = merge(this.condition, source.condition);
        setGroups(mergeModel(this.groups, source.groups));
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

    public Map<String, NamedCommandHooks> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, NamedCommandHooks> groups) {
        this.groups.clear();
        this.groups.putAll(groups);
    }

    public void addGroup(NamedCommandHooks hook) {
        this.groups.put(hook.getName(), hook);
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

        List<Map<String, Object>> groups = this.groups.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!groups.isEmpty()) map.put("groups", groups);

        return map;
    }
}
